# Auth-service
<img src="https://github.com/puitiza/ecommerce/blob/feature/authentication_authorization/images/initializr_auth_service.png?raw=true">

About this Project is essential to handle user authentication and authorization to the system


## Connection MySQL

<img src="/Users/anthonypuitiza/Documents/SpringBootProjects/Microservices/ecommerce/auth-service/src/main/resources/images/mysql_auth.png">

this is command that I use on the image

```
mysql -u root -p
SELECT user, host FROM mysql.user;
```
## Curl

```
curl --location 'http://localhost:8040/auth/sign-in' \
--header 'Content-Type: application/json' \
--data '{
    "username": "puiti",
    "password": "12345678"
}'
```

<img src="/Users/anthonypuitiza/Documents/SpringBootProjects/Microservices/ecommerce/auth-service/src/main/resources/images/curl_login.png">

