#!/bin/bash

# Set up variables, using the Flask repository with example users
SVN_REPO_URL="https://svn.example.com/repo"
SVN_USERNAME="exampleuser"
SVN_PASSWORD="examplepassword"
GIT_REPO_URL="https://github.com/pallets/flask.git"
GIT_BRANCH="master"
GIT_CODE_DIR="git-code"
BUILD_DIR=$(mktemp -d)
EMAIL_RECIPIENT="you@example.com"

#Checkout the SVN repository
svn checkout --username $SVN_USERNAME --password $SVN_PASSWORD $SVN_REPO_URL $BUILD_DIR/svn

# Clone the Git repository
git clone --branch $GIT_BRANCH $GIT_REPO_URL $BUILD_DIR/$GIT_CODE_DIR

# Copy Git code into SVN working copy
cp -r $BUILD_DIR/$GIT_CODE_DIR/* $BUILD_DIR/svn/git-code/

# Build and test the code
cd $BUILD_DIR/svn
make install
make test
BUILD_RESULT=$?

if [ $BUILD_RESULT -eq 0 ]; then
  # Push changes to Git
  git add .
  git commit -m "Update from Jenkins"
  git push

  # Send success email
  echo "Build and test succeeded. Changes pushed to Git." | mail -s "Jenkins Build Success" $EMAIL_RECIPIENT
else
  # Send failure email
  echo "Build or test failed. Changes not pushed to Git." | mail -s "Jenkins Build Failure" $EMAIL_RECIPIENT
fi

# Clean up temporary files
rm -rf $BUILD_DIR
