package com.rezdy.lunch.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.reflect.TypeToken;
import com.rezdy.lunch.entity.Ingredient;
import com.rezdy.lunch.entity.ProfileReqIngExclude;
import com.rezdy.lunch.entity.Recipe;
import com.rezdy.lunch.service.LunchServiceImpl;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;


import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(value = LunchController.class)
public class LunchControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LunchServiceImpl lunchServiceImpl;

    @BeforeEach
    public void setupMock() {
        MockitoAnnotations.initMocks(this);
    }

    //test case 1.1
    @Test
    public void getByDateNormalCase() throws Exception {

        List<Recipe> listRecipeMock = getRecipesMock();

        Mockito.when(lunchServiceImpl.getNonExpiredRecipesOnDate(any(LocalDate.class))).thenReturn(listRecipeMock);

        String inputMockDate="2020-01-01";
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/lunch").param("date",inputMockDate);
        MvcResult result = mockMvc.perform(requestBuilder).andReturn();
        assertEquals(200,result.getResponse().getStatus());
        assertResult(result);
    }

    //test case 2.1
    @Test
    public void getByRecipeNormalCase() throws Exception {

        List<Recipe> listRecipeMock = getRecipesMock();

        Mockito.when(lunchServiceImpl.getRecipe(any(String.class))).thenReturn(listRecipeMock);

        String inputMockRecipeTitle="Salad";
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/lunch/recipe").param("recipe",inputMockRecipeTitle);

        MvcResult result = mockMvc.perform(requestBuilder).andReturn();

        assertEquals(200,result.getResponse().getStatus());
        //use gsonBuilder for parsing list of objects
        assertResult(result);
    }


    //test case 3.1
    @Test
    public void getRecipeByExcludeIngredientNormalCase() throws Exception {

        List<Recipe> listRecipeMock = getRecipesMock();

        String exampleProfileAddReqJson = "{ \"tag\": [\"Bacon\",\"Bread\",\"Eggs\"]}";

        Mockito.when(lunchServiceImpl.excludeRecipeByIngre(any(ProfileReqIngExclude.class))).thenReturn(listRecipeMock);

        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .post("/lunch/exclueIngre").accept(MediaType.APPLICATION_JSON).content(exampleProfileAddReqJson)
                .contentType(MediaType.APPLICATION_JSON);;

        MvcResult result = mockMvc.perform(requestBuilder).andReturn();

        assertEquals(200,result.getResponse().getStatus());
        assertResult(result);
    }

    private List<Recipe> getRecipesMock() {
        Ingredient ingredientMoc1=new Ingredient("ingreTitle1",LocalDate.parse("2021-01-01"),LocalDate.parse("2021-01-01"));
        Ingredient ingredientMoc2=new Ingredient("ingreTitle2",LocalDate.parse("2021-02-01"),LocalDate.parse("2021-02-01"));
        Ingredient ingredientMoc3=new Ingredient("ingreTitle3",LocalDate.parse("2021-03-01"),LocalDate.parse("2021-03-01"));

        Set<Ingredient> ingresListMock=new HashSet<>();
        ingresListMock.add(ingredientMoc1);
        ingresListMock.add(ingredientMoc2);
        ingresListMock.add(ingredientMoc3);

        Recipe recipeMock1=new Recipe("recipeTitle1",ingresListMock);
        Recipe recipeMock2=new Recipe("recipeTitle2",ingresListMock);

        List<Recipe> listRecipeMock=new ArrayList<>();
        listRecipeMock.add(recipeMock1);
        listRecipeMock.add(recipeMock2);
        return listRecipeMock;
    }
    private void assertResult(MvcResult result) throws UnsupportedEncodingException {
        Gson gson = new GsonBuilder().registerTypeAdapter(LocalDate.class, (JsonDeserializer<LocalDate>) (json, type, jsonDeserializationContext) ->
                LocalDate.parse(json.getAsString(), DateTimeFormatter.ofPattern("yyyy-MM-dd"))).create();
        Type fooType=new TypeToken<List<Recipe>>(){}.getType();
        List<Recipe> recipes = gson.fromJson(result.getResponse().getContentAsString(), fooType);
        assertEquals(2,recipes.size());
        assertEquals("recipeTitle1",recipes.get(0).getTitle());
        String titleJoinRes=String.join(",", recipes.stream().map(x -> x.getTitle()).collect(Collectors.toList()));
        assertEquals("recipeTitle1,recipeTitle2",titleJoinRes);
        List<List>  titleList=recipes.stream().map(x -> x.getIngredients().stream().map(y -> y.getTitle()).collect(Collectors.toList())).collect(Collectors.toList());
        assertEquals("ingreTitle1",titleList.get(0).get(0));
    }
}
