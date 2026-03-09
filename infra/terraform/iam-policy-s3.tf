resource "aws_iam_policy" "ms_video_s3" {
  name = "ms-video-s3-policy"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect = "Allow"
      Action = [
        "s3:PutObject",
        "s3:GetObject",
        "s3:DeleteObject",
        "s3:ListBucket"
      ]
      Resource = [
        data.terraform_remote_state.s3.outputs.video_bucket_arn,
        "${data.terraform_remote_state.s3.outputs.video_bucket_arn}/*"
      ]
    }]
  })
}

resource "aws_iam_role_policy_attachment" "s3_attach" {
  role       = aws_iam_role.ms_video_irsa.name
  policy_arn = aws_iam_policy.ms_video_s3.arn
}
