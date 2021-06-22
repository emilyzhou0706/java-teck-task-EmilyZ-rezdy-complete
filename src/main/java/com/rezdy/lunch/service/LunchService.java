package com.rezdy.lunch.service;

import com.rezdy.lunch.entity.ProfileReqIngExclude;
import com.rezdy.lunch.entity.Recipe;

import java.time.LocalDate;
import java.util.List;

public interface LunchService {
    List<Recipe> getNonExpiredRecipesOnDate(LocalDate date);
    List<Recipe> loadRecipes(LocalDate date);
    List<Recipe> getRecipe(String recipe);
    List<Recipe> excludeRecipeByIngre(ProfileReqIngExclude excludeIngre);
    }
