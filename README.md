Lunch Microservice
The service provides an endpoint that will determine, from a set of recipes, what I can have for lunch at a given date, based on my fridge ingredient's expiry date, so that I can quickly decide what I’ll be having to eat, and the ingredients required to prepare the meal.

Prerequisites
Java 11 Runtime
Docker & Docker-Compose
Note: Docker is used for the local MySQL database instance, feel free to use your own instance or any other SQL database and insert data from lunch-data.sql script

Run
1.Start database:

docker-compose up -d
2.Add test data from sql/lunch-data.sql to the database. Here's a helper script if you prefer:

```
CONTAINER_ID=$(docker inspect --format="{{.Id}}" lunch-db)
```

```
docker cp sql/lunch-data.sql $CONTAINER_ID:/lunch-data.sql
```

```
docker exec $CONTAINER_ID /bin/sh -c 'mysql -u root -prezdytechtask lunch </lunch-data.sql'
```
3.Run Springboot LunchApplication
java -jar LunchApplication

4.URL for application
4.1 to get all recipes by date to with ingredients before use by date example
http://localhost:8080/lunch?date=2010-01-01
4.2 to get a recipes by recipes name example
http://localhost:8080/lunch/recipe?recipe=Salad
4.3 to get recipes by excluding ingredients example
http://localhost:8080/lunch/exclueIngre

5.Technology design of the application

GIVEN that I am a consumer of the Lunch API AND have made a GET request to the /lunch
endpoint with a given date THEN I should receive a JSON response of the recipes that I can
prepare based on the availability of the ingredients in my fridge

● GIVEN that I am a consumer of the Lunch API AND I have made a GET request to the /lunch
endpoint AND an ingredient is past its useBy date according to the date parameter THEN I
should not receive any recipes containing this ingredient

● GIVEN that I am a consumer of the Lunch API AND I have made a GET request to the /lunch
endpoint AND an ingredient is past its bestBefore date according to the date parameter
AND is still within its useBy date THEN any recipe containing this ingredient should be sorted to
the bottom of the JSON response object

● GIVEN that I am a consumer of the Lunch API, I want to look up a recipe by its title. Create a new
REST endpoint to lookup recipe by title AND return HTTP 404 status if the requested receipt
cannot be found.
GIVEN that I am a consumer of the Lunch API, I want to exclude receipts by a given set of
ingredients. Create a new REST endpoint to filter receipts by provided ingredientsSubmission





