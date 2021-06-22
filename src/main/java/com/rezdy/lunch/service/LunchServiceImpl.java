package com.rezdy.lunch.service;

import com.rezdy.lunch.entity.Ingredient;
import com.rezdy.lunch.entity.ProfileReqIngExclude;
import com.rezdy.lunch.entity.Recipe;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.criteria.*;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LunchServiceImpl implements LunchService{

    @Autowired
    private EntityManager entityManager;

    private List<Recipe> recipesSorted;

    @Override
    public List<Recipe> getNonExpiredRecipesOnDate(LocalDate date) {
        List<Recipe> recipes = loadRecipes(date);
        sortRecipes(recipes);

        return recipesSorted;
    }

    private void sortRecipes(List<Recipe> recipes) {
//        recipesSorted = recipes; //TODO sort recipes considering best-before
        recipesSorted=recipes.stream().sorted(
        (r1,r2)->-r1.getIngredients().stream().min(
                Comparator.comparing(Ingredient::getBestBefore)
        ).get().getBestBefore().compareTo(r2.getIngredients().stream().min(
                Comparator.comparing(Ingredient::getBestBefore)
        ).get().getBestBefore())
        ).collect(Collectors.toList());
    }

    @Override
    public List<Recipe> loadRecipes(LocalDate date) {
        //to get all recipes without expired ingredients
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Recipe> criteriaQuery = cb.createQuery(Recipe.class);
        Root<Recipe> recipeRoot = criteriaQuery.from(Recipe.class);

        CriteriaQuery<Recipe> query = criteriaQuery.select(recipeRoot);
        // work till here/////////////
        Subquery<Recipe> nonExpiredIngredientSubquery = query.subquery(Recipe.class);
        Root<Recipe> nonExpiredIngredient = nonExpiredIngredientSubquery.from(Recipe.class);

        nonExpiredIngredientSubquery.select(nonExpiredIngredient);

        Predicate matchingRecipe = cb.equal(nonExpiredIngredient.get("title"), recipeRoot.get("title"));
        Predicate expiredIngredient = cb.lessThan(nonExpiredIngredient.join("ingredients").get("useBy"), date);

        Predicate allNonExpiredIngredients = cb.not(cb.exists(nonExpiredIngredientSubquery.where(matchingRecipe, expiredIngredient)));

        return entityManager.createQuery(query.where(allNonExpiredIngredients)).getResultList();
    }

    @Override
    public List<Recipe> getRecipe(String recipe) {
        //to get recipe by recipe name
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Recipe> criteriaQuery = cb.createQuery(Recipe.class);
        Root<Recipe> recipeRoot = criteriaQuery.from(Recipe.class);

        criteriaQuery.where( cb.equal( recipeRoot.get( "title" ), recipe ) );
        return entityManager.createQuery( criteriaQuery ).getResultList();
    }

    @Override
    public List<Recipe> excludeRecipeByIngre(ProfileReqIngExclude excludeIngre){
        //to exclude any recipe containing any of the input ingredients
        final String parameterizedQuery = "select * from recipe where recipe.title not in (select distinct(recipe) from recipe_ingredient as ri inner join ingredient as i on ri.ingredient=i.TITLE where ingredient in(:excludeValue))";
        return entityManager.createNativeQuery(parameterizedQuery)
                .setParameter("excludeValue", excludeIngre.getTag())
                .getResultList();
    }

}
