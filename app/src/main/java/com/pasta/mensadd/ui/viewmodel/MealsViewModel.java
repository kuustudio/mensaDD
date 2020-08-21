package com.pasta.mensadd.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.pasta.mensadd.database.entity.Canteen;
import com.pasta.mensadd.database.entity.Meal;
import com.pasta.mensadd.database.repository.CanteenRepository;
import com.pasta.mensadd.database.repository.MealRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MealsViewModel extends ViewModel {

    private MealRepository mMealRepository;
    private CanteenRepository mCanteenRepository;
    private Map<String, LiveData<List<Meal>>> meals = new HashMap<>();
    private String mCanteenId;
    private MutableLiveData<Boolean> mCanteenIsFavorite;

    public MealsViewModel(MealRepository mealRepository, CanteenRepository canteenRepository, String canteenId) {
        mMealRepository = mealRepository;
        mCanteenRepository = canteenRepository;
        mCanteenId = canteenId;
        mCanteenIsFavorite = new MutableLiveData<>(false);
    }

    public LiveData<Canteen> getCanteenAsLiveData() {
        return mCanteenRepository.getCanteenById(mCanteenId);
    }

    public void toggleCanteenAsFavorite() {
        mCanteenRepository.toggleCanteenFavorite(mCanteenId);
    }

    public LiveData<List<Meal>> getMealsByDay(String day) {
        if (!meals.containsKey(day)) {
            meals.put(day, mMealRepository.getMealsByCanteenByDay(mCanteenId, day));
        }
        return meals.get(day);
    }

    public LiveData<Integer> getFetchState() {
        return mMealRepository.getFetchState();
    }

    public void triggerMealFetching() {
        mMealRepository.fetchMeals(mCanteenId);
    }

}
