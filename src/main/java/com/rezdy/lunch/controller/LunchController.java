package com.rezdy.lunch.controller;

import com.rezdy.lunch.service.LunchService;
import com.rezdy.lunch.service.Recipe;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
public class LunchController {

    private LunchService lunchService;

    @Autowired
    public LunchController(LunchService lunchService) {
        this.lunchService = lunchService;
    }

    @PostMapping("/lunch")
    public List<Recipe> getRecipes(@RequestParam(value = "date") String date) {
        return lunchService.getNonExpiredRecipesOnDate(LocalDate.parse(date));
    }
}
