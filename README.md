This is C++ News Bot which is able to collect news from multiple data sources like:

- Best of day/week/month/year StackOverflow question;
- [ACCU Overload](https://accu.org/index.php/journals/c78/) journals update;
- [BOOST](https://www.boost.org/) library updates.

News Bot will support some extra data sources soon:

- CMake releases;
- OpenSSL updates;
- libCURL updates;
- Starred items from [awesome-cpp](https://github.com/fffaraz/awesome-cpp).

# Compiling

The news bot is AWS Lambda hosted application and JVM 8 is **mandatory** for AWS execution. News Bot **must** be compiled with JVM 8, otherwise AWS Lambda will fail on startup inside `nio`. 

Compile `uberjar`:
```bash
lein uberjar
```

# Upload

Upload new version:
```bash
aws s3 cp .\target\uberjar\news-bot.jar s3://cpp-news-bot-singapore
```

Refresh AWS Lambda:
```bash
aws lambda update-function-code --function-name cpp-news-bot-ap-southeast-1 --region ap-southeast-1 --s3-bucket cpp-news-bot-singapore --s3-key news-bot.jar
```

Run AWS Lambda function once:
```bash
aws lambda invoke --invocation-type RequestResponse --function-name cpp-news-bot-ap-southeast-1 --region ap-southeast-1 --log-type Tail --payload '{}' outfile.txt
```

_NOTE:_ Terraform support is under construction.

# Testing

Some pre-configuration is required for local tests execution:

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

* Use `Cloverage` tool for coverage report generation:
```bash
 lein cloverage
 ```
