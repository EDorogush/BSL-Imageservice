provider "aws" {
  region = var.region
}

data "aws_region" "current" {}

resource "aws_s3_bucket" "this" {
  bucket        = "${data.aws_region.current.name}-images-bucket"
  acl           = "private"
  force_destroy = true

  tags = {
    Type = "Image"
  }
}

data "aws_iam_policy_document" "s3_policy" {
  statement {
    actions = [
      "s3:GetObject",
      "s3:PutObject",
      "s3:DeleteObject",
      "s3:DeleteObject",
      "s3:DeleteBucket",
      "s3:CreateBucket",
      "s3:ListBucket",
    ]
    resources = [
      "${aws_s3_bucket.this.arn}/*",
      aws_s3_bucket.this.arn
    ]
  }
}

resource "aws_iam_user" "user" {
  name = "test-user"
}

resource "aws_iam_policy" "policy" {
  name        = "test-policy"
  description = "A test policy"
  policy      =  data.aws_iam_policy_document.s3_policy.json
}

resource "aws_iam_user_policy_attachment" "test-attach" {
  user       = aws_iam_user.user.name
  policy_arn = aws_iam_policy.policy.arn
}


