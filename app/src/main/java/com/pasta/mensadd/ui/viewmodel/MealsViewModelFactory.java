package com.pasta.mensadd.ui.viewmodel;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.lifecycle.AbstractSavedStateViewModelFactory;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import androidx.savedstate.SavedStateRegistryOwner;

import com.pasta.mensadd.database.repository.CanteenRepository;
import com.pasta.mensadd.database.repository.MealRepository;

public class MealsViewModelFactory extends AbstractSavedStateViewModelFactory {

    private MealRepository mMealRepository;
    private CanteenRepository mCanteenRepository;

    public MealsViewModelFactory(SavedStateRegistryOwner savedStateRegistryOwner, Bundle bundle, MealRepository mealRepository, CanteenRepository canteenRepository) {
        super(savedStateRegistryOwner, bundle);
        mMealRepository = mealRepository;
        mCanteenRepository = canteenRepository;
    }


    @NonNull
    @Override
    protected <T extends ViewModel> T create(@NonNull String key, @NonNull Class<T> modelClass, @NonNull SavedStateHandle handle) {
        return (T) new MealsViewModel(mMealRepository, mCanteenRepository, handle);
    }
}
