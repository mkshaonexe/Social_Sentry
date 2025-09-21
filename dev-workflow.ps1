# Professional Development Workflow Script for Social Sentry (PowerShell)
# This script automates common development tasks for professional Git workflow

param(
    [Parameter(Position=0)]
    [string]$Command,
    
    [Parameter(Position=1)]
    [string]$Argument
)

# Colors for output
$Red = "Red"
$Green = "Green"
$Yellow = "Yellow"
$Blue = "Blue"

# Function to print colored output
function Write-Status {
    param([string]$Message)
    Write-Host "[INFO] $Message" -ForegroundColor $Blue
}

function Write-Success {
    param([string]$Message)
    Write-Host "[SUCCESS] $Message" -ForegroundColor $Green
}

function Write-Warning {
    param([string]$Message)
    Write-Host "[WARNING] $Message" -ForegroundColor $Yellow
}

function Write-Error {
    param([string]$Message)
    Write-Host "[ERROR] $Message" -ForegroundColor $Red
}

# Function to create a new feature branch
function New-Feature {
    param([string]$FeatureName)
    
    if ([string]::IsNullOrEmpty($FeatureName)) {
        Write-Error "Please provide a feature name"
        Write-Host "Usage: .\dev-workflow.ps1 feature <feature-name>"
        exit 1
    }
    
    $BranchName = "feature/$FeatureName"
    
    Write-Status "Creating feature branch: $BranchName"
    
    # Ensure we're on develop branch
    git checkout develop
    git pull origin develop
    
    # Create and checkout new feature branch
    git checkout -b $BranchName
    
    Write-Success "Feature branch '$BranchName' created successfully"
    Write-Status "You can now start developing your feature"
}

# Function to commit changes with conventional commit format
function Submit-Commit {
    param([string]$Message)
    
    if ([string]::IsNullOrEmpty($Message)) {
        Write-Error "Please provide a commit message"
        Write-Host "Usage: .\dev-workflow.ps1 commit <message>"
        exit 1
    }
    
    Write-Status "Staging all changes..."
    git add .
    
    Write-Status "Committing changes..."
    git commit -m $Message
    
    Write-Success "Changes committed successfully"
}

# Function to push feature branch
function Push-Feature {
    $CurrentBranch = git branch --show-current
    
    if ($CurrentBranch -notmatch "^feature/") {
        Write-Error "You must be on a feature branch to push"
        exit 1
    }
    
    Write-Status "Pushing feature branch: $CurrentBranch"
    git push origin $CurrentBranch
    
    Write-Success "Feature branch pushed successfully"
    Write-Status "Create a pull request at: https://github.com/mkshaonexe/Social_Sentry/pull/new/$CurrentBranch"
}

# Function to merge feature to develop
function Merge-Feature {
    $CurrentBranch = git branch --show-current
    
    if ($CurrentBranch -notmatch "^feature/") {
        Write-Error "You must be on a feature branch to merge"
        exit 1
    }
    
    Write-Status "Switching to develop branch..."
    git checkout develop
    git pull origin develop
    
    Write-Status "Merging feature branch: $CurrentBranch"
    git merge $CurrentBranch --no-ff -m "Merge $CurrentBranch into develop"
    
    Write-Status "Pushing develop branch..."
    git push origin develop
    
    Write-Status "Cleaning up feature branch..."
    git branch -d $CurrentBranch
    git push origin --delete $CurrentBranch
    
    Write-Success "Feature merged successfully and branch cleaned up"
}

# Function to run tests and build
function Test-AndBuild {
    Write-Status "Running tests..."
    .\gradlew.bat test
    
    Write-Status "Building project..."
    .\gradlew.bat build
    
    Write-Success "Tests passed and build successful"
}

# Function to show current status
function Show-Status {
    Write-Status "Current Git Status:"
    git status
    
    Write-Host ""
    Write-Status "Recent commits:"
    git log --oneline -5
    
    Write-Host ""
    Write-Status "Branch information:"
    git branch -v
}

# Function to setup development environment
function Initialize-Dev {
    Write-Status "Setting up development environment..."
    
    # Configure Git
    git config commit.template .gitmessage
    git config pull.rebase false
    git config core.autocrlf true
    
    # Create develop branch if it doesn't exist
    $DevelopExists = git show-ref --verify --quiet refs/heads/develop
    if ($LASTEXITCODE -ne 0) {
        git checkout -b develop
        git push origin develop
        Write-Success "Created develop branch"
    } else {
        git checkout develop
        Write-Status "Switched to existing develop branch"
    }
    
    Write-Success "Development environment setup complete"
}

# Main script logic
switch ($Command.ToLower()) {
    "setup" {
        Initialize-Dev
    }
    "feature" {
        New-Feature $Argument
    }
    "commit" {
        Submit-Commit $Argument
    }
    "push" {
        Push-Feature
    }
    "merge" {
        Merge-Feature
    }
    "test" {
        Test-AndBuild
    }
    "status" {
        Show-Status
    }
    default {
        Write-Host "Professional Development Workflow Script"
        Write-Host ""
        Write-Host "Usage: .\dev-workflow.ps1 <command> [arguments]"
        Write-Host ""
        Write-Host "Commands:"
        Write-Host "  setup                    - Setup development environment"
        Write-Host "  feature <name>           - Create new feature branch"
        Write-Host "  commit <message>         - Commit changes with message"
        Write-Host "  push                     - Push current feature branch"
        Write-Host "  merge                    - Merge feature to develop"
        Write-Host "  test                     - Run tests and build"
        Write-Host "  status                   - Show current status"
        Write-Host ""
        Write-Host "Examples:"
        Write-Host "  .\dev-workflow.ps1 setup"
        Write-Host "  .\dev-workflow.ps1 feature add-dark-mode"
        Write-Host "  .\dev-workflow.ps1 commit 'feat(ui): add dark mode toggle'"
        Write-Host "  .\dev-workflow.ps1 push"
        Write-Host "  .\dev-workflow.ps1 merge"
    }
}
