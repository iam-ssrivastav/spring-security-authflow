#!/bin/bash

echo "======================================"
echo "  AuthFlow - Quick Start Script"
echo "======================================"
echo ""

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "❌ Java is not installed. Please install Java 17 or higher."
    exit 1
fi

echo "✅ Java found: $(java -version 2>&1 | head -n 1)"
echo ""

# Build the project
echo "📦 Building the project..."
./mvnw clean install -DskipTests

if [ $? -ne 0 ]; then
    echo "❌ Build failed. Please check the errors above."
    exit 1
fi

echo ""
echo "✅ Build successful!"
echo ""

# Run the application
echo "🚀 Starting AuthFlow application..."
echo ""
echo "Application will be available at: http://localhost:8080"
echo ""
echo "Demo Users:"
echo "  - Username: user     | Password: password123 | Role: USER"
echo "  - Username: manager  | Password: password123 | Role: MANAGER"
echo "  - Username: admin    | Password: password123 | Role: ADMIN"
echo ""
echo "Press Ctrl+C to stop the application"
echo "======================================"
echo ""

./mvnw spring-boot:run
