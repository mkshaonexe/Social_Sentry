#!/bin/bash

# Professional Development Workflow Script for Social Sentry
# This script automates common development tasks for professional Git workflow

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to create a new feature branch
create_feature() {
    if [ -z "$1" ]; then
        print_error "Please provide a feature name"
        echo "Usage: $0 feature <feature-name>"
        exit 1
    fi
    
    local feature_name="$1"
    local branch_name="feature/$feature_name"
    
    print_status "Creating feature branch: $branch_name"
    
    # Ensure we're on develop branch
    git checkout develop
    git pull origin develop
    
    # Create and checkout new feature branch
    git checkout -b "$branch_name"
    
    print_success "Feature branch '$branch_name' created successfully"
    print_status "You can now start developing your feature"
}

# Function to commit changes with conventional commit format
commit_changes() {
    if [ -z "$1" ]; then
        print_error "Please provide a commit message"
        echo "Usage: $0 commit <message>"
        exit 1
    fi
    
    local message="$1"
    
    print_status "Staging all changes..."
    git add .
    
    print_status "Committing changes..."
    git commit -m "$message"
    
    print_success "Changes committed successfully"
}

# Function to push feature branch
push_feature() {
    local current_branch=$(git branch --show-current)
    
    if [[ ! "$current_branch" =~ ^feature/ ]]; then
        print_error "You must be on a feature branch to push"
        exit 1
    fi
    
    print_status "Pushing feature branch: $current_branch"
    git push origin "$current_branch"
    
    print_success "Feature branch pushed successfully"
    print_status "Create a pull request at: https://github.com/mkshaonexe/Social_Sentry/pull/new/$current_branch"
}

# Function to merge feature to develop
merge_feature() {
    local current_branch=$(git branch --show-current)
    
    if [[ ! "$current_branch" =~ ^feature/ ]]; then
        print_error "You must be on a feature branch to merge"
        exit 1
    fi
    
    print_status "Switching to develop branch..."
    git checkout develop
    git pull origin develop
    
    print_status "Merging feature branch: $current_branch"
    git merge "$current_branch" --no-ff -m "Merge $current_branch into develop"
    
    print_status "Pushing develop branch..."
    git push origin develop
    
    print_status "Cleaning up feature branch..."
    git branch -d "$current_branch"
    git push origin --delete "$current_branch"
    
    print_success "Feature merged successfully and branch cleaned up"
}

# Function to run tests and build
test_and_build() {
    print_status "Running tests..."
    ./gradlew test
    
    print_status "Building project..."
    ./gradlew build
    
    print_success "Tests passed and build successful"
}

# Function to show current status
show_status() {
    print_status "Current Git Status:"
    git status
    
    echo ""
    print_status "Recent commits:"
    git log --oneline -5
    
    echo ""
    print_status "Branch information:"
    git branch -v
}

# Function to setup development environment
setup_dev() {
    print_status "Setting up development environment..."
    
    # Configure Git
    git config commit.template .gitmessage
    git config pull.rebase false
    git config core.autocrlf true
    
    # Create develop branch if it doesn't exist
    if ! git show-ref --verify --quiet refs/heads/develop; then
        git checkout -b develop
        git push origin develop
        print_success "Created develop branch"
    else
        git checkout develop
        print_status "Switched to existing develop branch"
    fi
    
    print_success "Development environment setup complete"
}

# Main script logic
case "$1" in
    "setup")
        setup_dev
        ;;
    "feature")
        create_feature "$2"
        ;;
    "commit")
        commit_changes "$2"
        ;;
    "push")
        push_feature
        ;;
    "merge")
        merge_feature
        ;;
    "test")
        test_and_build
        ;;
    "status")
        show_status
        ;;
    *)
        echo "Professional Development Workflow Script"
        echo ""
        echo "Usage: $0 <command> [arguments]"
        echo ""
        echo "Commands:"
        echo "  setup                    - Setup development environment"
        echo "  feature <name>           - Create new feature branch"
        echo "  commit <message>         - Commit changes with message"
        echo "  push                     - Push current feature branch"
        echo "  merge                    - Merge feature to develop"
        echo "  test                     - Run tests and build"
        echo "  status                   - Show current status"
        echo ""
        echo "Examples:"
        echo "  $0 setup"
        echo "  $0 feature add-dark-mode"
        echo "  $0 commit 'feat(ui): add dark mode toggle'"
        echo "  $0 push"
        echo "  $0 merge"
        ;;
esac
