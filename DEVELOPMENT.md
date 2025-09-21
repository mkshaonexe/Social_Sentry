# Social Sentry - Development Guidelines

## Git Workflow

### Branch Strategy
- `main`: Production-ready code
- `develop`: Integration branch for features
- `feature/*`: New features and improvements
- `hotfix/*`: Critical bug fixes

### Commit Message Format
Use conventional commits format:
```
<type>(<scope>): <subject>

<body>

<footer>
```

Types: feat, fix, docs, style, refactor, perf, test, chore

### Development Process
1. Create feature branch from `develop`
2. Make changes with proper commits
3. Create pull request to `develop`
4. Code review and merge
5. Deploy to `main` when ready

## Code Quality
- Follow Kotlin coding conventions
- Use meaningful variable and function names
- Add comments for complex logic
- Write unit tests for new features
- Ensure accessibility compliance

## Testing
- Unit tests for business logic
- Integration tests for services
- Manual testing on real devices
- Test accessibility service functionality

## Performance
- Optimize for battery usage
- Minimize memory footprint
- Efficient accessibility service monitoring
- Smooth UI animations

## Security & Privacy
- No network permissions
- Local data storage only
- No user tracking
- Secure data handling
