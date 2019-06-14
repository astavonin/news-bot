variable "lambda_payload_filename" {
  default = "../target/uberjar/news-bot.jar"
}

variable "lambda_function_handler" {
  default = "news-bot.lambda.LambdaFn"
}

variable "lambda_runtime" {
  default = "java8"
}

variable "lambda_memory" {
  default = "512"
}

variable "region" {
  default = "us-west-2"
}

variable "lambda_timeout" {
  default = 900
}

variable "s3_storage" {
  default = "cpp-news-bot-us-west-2"
}