output "aws-cli" {
  value = "aws lambda invoke --invocation-type RequestResponse --function-name ${var.lambda_function_handler} --region ${var.region} --log-type Tail --payload '{\"some\":\"input\"}' outfile.txt"
}
