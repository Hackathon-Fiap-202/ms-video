resource "aws_iam_policy" "ms_video_ssm" {
  name = "ms-video-ssm-policy"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect = "Allow"
      Action = [
        "ssm:GetParameter",
        "ssm:GetParameters"
      ]
      Resource = "arn:aws:ssm:${var.aws_region}:500782030170:parameter/ms-video/*"
    }]
  })
}

resource "aws_iam_role_policy_attachment" "ssm_attach" {
  role       = aws_iam_role.ms_video_irsa.name
  policy_arn = aws_iam_policy.ms_video_ssm.arn
}
