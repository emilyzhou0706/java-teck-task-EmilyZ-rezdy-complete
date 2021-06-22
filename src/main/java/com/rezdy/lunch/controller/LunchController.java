package com.rezdy.lunch.controller;

import com.rezdy.lunch.entity.ProfileReqIngExclude;
import com.rezdy.lunch.entity.Recipe;
import com.rezdy.lunch.exception.ErrorResponse;
import com.rezdy.lunch.service.LunchServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@RestController
public class LunchController {

    private LunchServiceImpl lunchService;

    @Autowired
    public LunchController(LunchServiceImpl lunchService) {
        this.lunchService = lunchService;
    }

    @GetMapping("/lunch")
    public List<Recipe> getRecipes(@RequestParam(value = "date") String date) {
        return lunchService.getNonExpiredRecipesOnDate(LocalDate.parse(date));
    }

    @GetMapping("/lunch/recipe")
    public @ResponseBody ResponseEntity<List<Recipe>> getRecipeByName(@RequestParam(value = "recipe") String myRecipe) {
        List<Recipe> recipes=lunchService.getRecipe(myRecipe);
        if(!(recipes.size()==0))
            {return ResponseEntity.ok(recipes);}
        else{
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "recipe not found"
            );
        }
    }

    @PostMapping("/lunch/exclueIngre")
    public @ResponseBody List<Recipe> excludeRecipeByIngre(@RequestBody ProfileReqIngExclude exclueIngre) {
        return lunchService.excludeRecipeByIngre(exclueIngre);
    }

}
