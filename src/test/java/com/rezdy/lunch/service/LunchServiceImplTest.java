package com.rezdy.lunch.service;

import com.rezdy.lunch.entity.Ingredient;
import com.rezdy.lunch.entity.ProfileReqIngExclude;
import com.rezdy.lunch.entity.Recipe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class LunchServiceImplTest {
    @InjectMocks
    private LunchServiceImpl lunchServiceImpl;

    @Mock
    EntityManager entityManager;

    @Mock
    CriteriaBuilder                             cb;
    @Mock
    CriteriaQuery<Recipe>                cq;
    @Mock
    Root<Recipe> rootRecipeEntity;
    @Mock
    Path path;

    @BeforeEach
    public void setupMock() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void getNonExpiredRecipesOnDateTest(){
        LocalDate localDateMock=LocalDate.parse("2020-01-01");
        loadRecipesTest();
        List<Recipe> recipesList=lunchServiceImpl.getNonExpiredRecipesOnDate(localDateMock);
        //assert the recipe is reversed accrding to bestbefore date
        assertEquals("recipeTitle2",recipesList.get(0).getTitle());
    }

    @Test
    public void sortRecipesTest() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        List<Recipe> listRecipeMock = getRecipesMock();
        Method method = LunchServiceImpl.class.getDeclaredMethod("sortRecipes",
                List.class);
        method.setAccessible(true);
        method.invoke(lunchServiceImpl, listRecipeMock);
    }

    @Test
    public void getRecipeTest()  {
        List<Recipe> listRecipeMock = getRecipesMock();
        when(entityManager.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createQuery(Recipe.class)).thenReturn(cq);
        when(cq.from(Recipe.class)).thenReturn(rootRecipeEntity);
        when(rootRecipeEntity.get( "title")).thenReturn(path);
        Predicate lastNameIsLikePredicateMock = mock(Predicate.class);
        when(cb.equal( rootRecipeEntity.get( "title" ),listRecipeMock)).thenReturn(lastNameIsLikePredicateMock);
        when(cq.where(cb.equal( rootRecipeEntity.get( "title" ), listRecipeMock ))).thenReturn(cq);
        TypedQuery<Recipe> typedQuery = mock(TypedQuery.class);
        when(typedQuery.getResultList()).thenReturn(listRecipeMock);
        when(entityManager.createQuery(cq)).thenReturn(typedQuery);
        List<Recipe> recipeList=lunchServiceImpl.getRecipe("Salad");
        assertEquals("recipeTitle1",recipeList.get(0).getTitle());
    }

    @Test
    public void loadRecipesTest()  {
        LocalDate localDateMock=LocalDate.parse("2020-01-01");
        List<Recipe> listRecipeMock = getRecipesMock();
        when(entityManager.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createQuery(Recipe.class)).thenReturn(cq);
        when(cq.from(Recipe.class)).thenReturn(rootRecipeEntity);
        Query query = mock(Query.class);
        when(cq.select(rootRecipeEntity)).thenReturn(cq);
        Subquery nonExpiredIngredientSubquery = mock(Subquery.class);
        when(cq.subquery(Recipe.class)).thenReturn(nonExpiredIngredientSubquery);
        Root<Recipe> nonExpiredIngredient = mock(Root.class);
        when(nonExpiredIngredientSubquery.from(Recipe.class)).thenReturn(nonExpiredIngredient);
        when(nonExpiredIngredientSubquery.select(nonExpiredIngredient)).thenReturn(nonExpiredIngredientSubquery);
        when(nonExpiredIngredient.get("title")).thenReturn(path);
        when(rootRecipeEntity.get("title")).thenReturn(path);
        Predicate matchingRecipe = mock(Predicate.class);
        when(cb.equal(nonExpiredIngredient.get("title"), rootRecipeEntity.get("title"))).thenReturn(matchingRecipe);
        Join join = mock(Join.class);
        when(nonExpiredIngredient.join("ingredients")).thenReturn(join);
        when(nonExpiredIngredient.join("ingredients").get("useBy")).thenReturn(path);
        Predicate expiredIngredient = mock(Predicate.class);
        when(cb.lessThan(nonExpiredIngredient.join("ingredients").get("useBy"), localDateMock)).thenReturn(expiredIngredient);
        when(nonExpiredIngredientSubquery.where(matchingRecipe, expiredIngredient)).thenReturn(nonExpiredIngredientSubquery);
        Predicate intermediate = mock(Predicate.class);

        when(cb.exists(nonExpiredIngredientSubquery.where(matchingRecipe, expiredIngredient))).thenReturn(intermediate);
        Predicate allNonExpiredIngredients =mock(Predicate.class);
        when(cb.not(cb.exists(nonExpiredIngredientSubquery.where(matchingRecipe, expiredIngredient)))).thenReturn(allNonExpiredIngredients);
        when(cq.where(allNonExpiredIngredients)).thenReturn(cq);
        TypedQuery<Recipe> typedQuery = mock(TypedQuery.class);
        when(entityManager.createQuery(cq.where(allNonExpiredIngredients))).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(listRecipeMock);

        List<Recipe> recipeList=lunchServiceImpl.loadRecipes(localDateMock);
        assertEquals("recipeTitle1",recipeList.get(0).getTitle());
    }

    @Test
    public void excludeRecipeByIngreTest()  {
        List<Recipe> listRecipeMock = getRecipesMock();
        final String parameterizedQuery = "select * from recipe where recipe.title not in (select distinct(recipe) from recipe_ingredient as ri inner join ingredient as i on ri.ingredient=i.TITLE where ingredient in(:excludeValue))";
        ProfileReqIngExclude profileReqIngExclude=new ProfileReqIngExclude();
        profileReqIngExclude.setTag(Arrays.asList("Bacon","Bread","Eggs"));
        Query query = mock(Query.class);
        when(entityManager.createNativeQuery(parameterizedQuery)).thenReturn(query);
        when(query.setParameter("excludeValue",profileReqIngExclude.getTag())).thenReturn(query);
        when(query.getResultList()).thenReturn(listRecipeMock);
        List<Recipe> recipeList=lunchServiceImpl.excludeRecipeByIngre(profileReqIngExclude);
        assertEquals("recipeTitle1",recipeList.get(0).getTitle());
    }

    private List<Recipe> getRecipesMock() {
        Ingredient ingredientMoc1=new Ingredient("ingreTitle1", LocalDate.parse("2021-01-01"),LocalDate.parse("2021-01-01"));
        Ingredient ingredientMoc2=new Ingredient("ingreTitle2",LocalDate.parse("2021-02-01"),LocalDate.parse("2021-02-01"));
        Ingredient ingredientMoc3=new Ingredient("ingreTitle3",LocalDate.parse("2021-03-01"),LocalDate.parse("2021-03-01"));

        Ingredient ingredientMoc4=new Ingredient("ingreTitle1", LocalDate.parse("2031-01-01"),LocalDate.parse("2031-01-01"));
        Ingredient ingredientMoc5=new Ingredient("ingreTitle2",LocalDate.parse("2031-02-01"),LocalDate.parse("2031-02-01"));
        Ingredient ingredientMoc6=new Ingredient("ingreTitle3",LocalDate.parse("2031-03-01"),LocalDate.parse("2031-03-01"));

        Set<Ingredient> ingresListMock1=new HashSet<>();
        ingresListMock1.add(ingredientMoc1);
        ingresListMock1.add(ingredientMoc2);
        ingresListMock1.add(ingredientMoc3);

        Set<Ingredient> ingresListMock2=new HashSet<>();
        ingresListMock2.add(ingredientMoc4);
        ingresListMock2.add(ingredientMoc5);
        ingresListMock2.add(ingredientMoc6);

        Recipe recipeMock1=new Recipe("recipeTitle1",ingresListMock1);
        Recipe recipeMock2=new Recipe("recipeTitle2",ingresListMock2);

        List<Recipe> listRecipeMock=new ArrayList<>();
        listRecipeMock.add(recipeMock1);
        listRecipeMock.add(recipeMock2);
        return listRecipeMock;
    }
}
