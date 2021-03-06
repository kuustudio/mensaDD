package com.pasta.mensadd.ui.fragments;


import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.pasta.mensadd.R;
import com.pasta.mensadd.database.AppDatabase;
import com.pasta.mensadd.database.entity.BalanceEntry;
import com.pasta.mensadd.database.repository.BalanceEntryRepository;
import com.pasta.mensadd.ui.viewmodel.BalanceHistoryViewModel;
import com.pasta.mensadd.ui.viewmodel.BalanceHistoryViewModelFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import lecho.lib.hellocharts.formatter.SimpleAxisValueFormatter;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.view.ColumnChartView;
import lecho.lib.hellocharts.view.LineChartView;

public class BalanceHistoryFragment extends Fragment {

    private SimpleDateFormat mDateFormat;
    private TextView mCurrentBalance;
    private TextView mCurrentLastTransaction;
    private TextView noBalanceText;
    private TextView noTransactionText;
    private LineChartView mBalanceChart;
    private ColumnChartView mTransactionChart;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_balance_history, container, false);
        setHasOptionsMenu(false);
        Locale locale;
        String dateFormat;
        if (Locale.getDefault().getLanguage().equals("de")) {
            locale = Locale.GERMANY;
            dateFormat = "dd.MM.";
        } else {
            locale = Locale.ENGLISH;
            dateFormat = "MM-dd";
        }
        mDateFormat = new SimpleDateFormat(dateFormat, locale);
        mBalanceChart = v.findViewById(R.id.lineChart);
        mTransactionChart = v.findViewById(R.id.columnChart);
        mCurrentBalance = v.findViewById(R.id.currentBalance);
        mCurrentLastTransaction = v.findViewById(R.id.currentLastTransaction);
        noBalanceText = v.findViewById(R.id.notEnoughDataForLine);
        noTransactionText = v.findViewById(R.id.notEnoughDataForColumn);

        mTransactionChart.setZoomEnabled(false);
        mTransactionChart.setScrollEnabled(false);
        mTransactionChart.setClickable(false);
        mBalanceChart.setZoomEnabled(false);
        mBalanceChart.setScrollEnabled(false);
        mBalanceChart.setClickable(false);
        BalanceHistoryViewModelFactory balanceHistoryViewModelFactory = new BalanceHistoryViewModelFactory(new BalanceEntryRepository(AppDatabase.getInstance(requireContext())));
        BalanceHistoryViewModel balanceHistoryViewModel = new ViewModelProvider(this, balanceHistoryViewModelFactory).get(BalanceHistoryViewModel.class);
        balanceHistoryViewModel.getBalanceEntries().observe(getViewLifecycleOwner(), balanceEntries -> {
            updateBalanceHistory(true, balanceEntries);
        });

        return v;
    }

    public String formatMoneyString(String value) {
        String[] split = value.split("\\.");
        String euro = split[0];
        String cent = split[1];
        if (cent.length() < 2)
            cent += "0";
        return euro + "," + cent + "€";
    }


    public void setUpBalanceChart(List<BalanceEntry> balanceEntries) {
        List<PointValue> values = new ArrayList<>();
        List<AxisValue> axisValues = new ArrayList<>();
        for (int i = 0; i < balanceEntries.size(); i++) {
            values.add(new PointValue(i, balanceEntries.get(i).getCardBalance()));
            //if (mBalance.get(i) > mMaxBalance)
            //mMaxBalance = mBalance.get(i);
            Date date = new Date(balanceEntries.get(i).getTimestamp());
            axisValues.add(new AxisValue(i).setLabel(mDateFormat.format(date)));
        }

        Line line = new Line(values).setColor(getResources().getColor(R.color.pink_dark)).setCubic(false).setFilled(true).setHasPoints(false);
        List<Line> lines = new ArrayList<>();
        lines.add(line);


        LineChartData data = new LineChartData();

        Axis axisY = new Axis().setHasLines(true).setMaxLabelChars(4).setFormatter(new SimpleAxisValueFormatter().setAppendedText("€".toCharArray()));
        Axis axisX = new Axis(axisValues).setMaxLabelChars(5);


        data.setAxisYLeft(axisY);
        data.setAxisXBottom(axisX);
        data.setLines(lines);

        for (int j = 0; j < line.getValues().size(); j++) {
            line.getValues().get(j).setTarget(j, balanceEntries.get(j).getCardBalance());
        }
        mBalanceChart.setLineChartData(data);
    }

    public void setUpTransactionsChart(List<BalanceEntry> balanceEntries) {
        int numColumns = balanceEntries.size();
        List<AxisValue> axisValues = new ArrayList<>();
        List<Column> columns = new ArrayList<>();
        for (int i = 0; i < numColumns; ++i) {
            List<SubcolumnValue> values = new ArrayList<>();
            values.add(new SubcolumnValue(balanceEntries.get(i).getLastTransaction(), getResources().getColor(R.color.cyan_dark)));
            Date date = new Date(balanceEntries.get(i).getTimestamp());
            axisValues.add(new AxisValue(i).setLabel(mDateFormat.format(date)));

            Column column = new Column(values);
            columns.add(column);
        }

        ColumnChartData data = new ColumnChartData(columns);

        Axis axisY = new Axis().setHasLines(true).setMaxLabelChars(4).setFormatter(new SimpleAxisValueFormatter().setAppendedText("€".toCharArray()));

        data.setAxisXBottom(new Axis(axisValues).setMaxLabelChars(5));
        data.setAxisYLeft(axisY);

        mTransactionChart.setColumnChartData(data);

        for (int j = 0; j < data.getColumns().size(); j++) {
            for (SubcolumnValue value : data.getColumns().get(j).getValues()) {
                value.setTarget(balanceEntries.get(j).getLastTransaction());
            }
        }
    }

    public void updateBalanceHistory(boolean firstSetup, List<BalanceEntry> balanceEntries) {


        if (balanceEntries.size() > 1) {
            setUpBalanceChart(balanceEntries);
            setUpTransactionsChart(balanceEntries);
            noBalanceText.setVisibility(View.GONE);
            noTransactionText.setVisibility(View.GONE);
            if (firstSetup)
                animateGraphs();
        } else {
            if (balanceEntries.isEmpty()) {
                mCurrentBalance.setText(getString(R.string.balance_check_explanation));
            }
        }
    }

    public void animateGraphs() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mBalanceChart.startDataAnimation();
                mTransactionChart.startDataAnimation();
            }
        }, 500);
    }

}
