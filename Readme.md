# Prerequisites to developing
- java 8 jdk
- scala

# Prerequisites to run
- java 8 jre

# How to make
* git clone
* use gradle task "generateFiles" to generate source files (mappings and user labels). Feel free to change next parameters:
    - maxDistanceError (Double) default value 40000.0, distance error for each mapping point will be calculated as random in range [0, maxDistanceError) 
    - userCount (Int, >0) default value 100000. Please, do not apply more than 10000000, because it takes ~ 3 minutes to generate and load on start such file
    - mappingPointsFileName (String) default value mappings.txt
    - userLabelsFileName (String) default value user_labels.txt
    - example1: ``` ./gradlew -PmaxDistanceError=20000.0 -PuserCount=10000000 -PmappingPointsFileName=mappings.txt -PuserLabelsFileName=user_labels.txt generateFiles```
    - example2: ``` ./gradlew -PmaxDistanceError=10000.0 -PuserCount=10000000 generateFiles```
    - example4: ``` ./gradlew generateFiles```
* make an application Jar using ``` ./gradlew shadowJar ```
* you can find the artifact in ``` ./build/libs/ ```

# How to run
* get an application jar
* run it with java -jar. Do not hesitate to change application parameters:
    - http.host (String) default value 0.0.0.0
    - http.port=1000 (Int, port values) default 51012
    - dao.user-file-path (String)
    - dao.mapping-file-path (String)
* do not forget to give enough heap. For instance to work with 10000000 userLabels it can take up to 3Gb
* complete run command examples:
    - ``` java -Dhttp.host=127.0.0.1 -Dhttp.port=8080 -Ddao.user-file-path=user_labels.txt -Ddao.mapping-file-path=mappings.txt -Xmx3072m -jar build/libs/one_factor_test_task-1.0-SNAPSHOT-all.jar ```
    - ``` java -jar build/libs/one_factor_test_task-1.0-SNAPSHOT-all.jar ```
    
# HTTP API
### POST /user-label/upsert to insert or update user label
```json
{
    "userLabelId": 100500,
    "longitude": 30.43554,
    "latitude": -56.87246
}
```
    - userLabelId: Int
    - longitude: Double
    - latitude: Double
##### result:
    - 200 OK on OK
    - 500 Internal server error with error describing payload
    
### POST /user-label/delete to delete user label and user label index on some mapping point
```json
{
    "userLabelId": 100500
}
```
    - userLabelId: Int
##### result:
    - 200 OK on OK
    - 404 Not found if user label was not found
    - 500 Internal server error with error describing payload
    
### POST /user-label-nearness to get an answer(true/false) to the question "Is user near(with mapping point distance error precise of course) his mapping point or not"
```json
{
    "userLabelId": 100500
}
```
    - userLabelId: Int
##### result:
    - 200 OK with json:
        {
            "isNearMappingPoint": true
        }
        isNearMappingPoint: Boolean
    - 404 Not found if user label was not found
    - 500 Internal server error with error describing payload
    
### POST /geo-point-statistics to get a statistics about mapping point that is near the geo point you given
```json
{
    "longitude": -117.565,
    "latitude": -28
}
```
    - longitude: Double
    - latitude: Double
##### result:
    - 200 OK with json:
        {
            "userLabelCount": 4,
            "userLabelIds": [
                42,
                420,
                1452,
                97453
            ]
        }
        userLabelCount: Int (>=0)
        userLabelIds: Array of Int
    - 404 Not found if mapping point of geo point you given was not found
    - 500 Internal server error with error describing payload
    
