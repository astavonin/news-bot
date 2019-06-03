
# Testing

* _twitter.edn file should contain Twitter credentials as END record:
```clojure
{:AppKey "_APP_KEY_",
 :AppSecret "_APP_SECRET_",
 :UserToken "_USER_TOKEN_",
 :UserTokenSecret "_USER_TOKEN_SECRET_"}
```

* [LocalStack](https://localstack.cloud/) should be available as Docker image `localstack/localstack`.
```bash
docker-compose up
```

* For coverage report generation:
```bash
 lein cloverage
 ```
 
 * [Logo](https://github.com/isocpp/logos) is form isocpp.org.
 