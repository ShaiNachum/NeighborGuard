# NeighborGuard - Android Client

A native Android application that connects community volunteers with people requiring assistance through intelligent location-based matching and real-time service coordination.

## 📱 Frontend Overview

The NeighborGuard Android client provides an intuitive interface for community members to offer or request assistance within their neighborhoods. Built with native Android technologies, the application delivers optimal performance while maintaining accessibility for users across different technical proficiency levels.

### Core Client Features

The application supports two distinct user roles with specialized functionality for each user type. Volunteers can browse nearby recipients, view detailed assistance requests, create meeting arrangements, and track their community service history. Recipients can request specific assistance types, view available volunteers in their area, manage ongoing assistance sessions, and provide feedback upon service completion.

The user interface emphasizes simplicity and accessibility, incorporating Material Design principles to ensure consistent visual patterns and intuitive navigation flows. The application leverages device capabilities including GPS location services, camera integration for profile images, and push notifications for real-time updates.

## 🏗️ Android Architecture

### Application Structure

The client follows a modular architecture pattern with clear separation of concerns across presentation, business logic, and data access layers. Activities and Fragments handle user interface presentation, while dedicated service classes manage business logic and API communication. Adapters provide efficient data binding for RecyclerView components, enabling smooth scrolling and dynamic content updates.

### Key Components

**User Interface Layer**
- MainActivity serves as the primary navigation controller with bottom navigation and fragment management
- HomeFragment displays nearby assistance opportunities with integrated map and list views
- MeetingsFragment provides comprehensive meeting management and status tracking
- MeetingActivity handles detailed meeting interactions and status updates
- Authentication activities manage user registration and login workflows

**Data Management Layer**
- CurrentUserManager maintains session state and user preferences throughout the application lifecycle
- ApiController centralizes HTTP communication with the backend server
- LocationManager handles GPS services and geographic proximity calculations
- PermissionManager ensures proper handling of sensitive device capabilities

**Utility Components**
- Custom adapters for RecyclerView components including RecipientAdapter and MeetingAdapter
- DialogUtils for consistent modal interactions and user confirmations
- Image processing utilities leveraging Glide for efficient profile picture management

## 🛠️ Technology Stack

### Development Framework
- **Platform**: Android SDK with minimum API level 26 (Android 8.0)
- **Language**: Java with Android Studio development environment
- **Build System**: Gradle with Kotlin DSL configuration
- **UI Framework**: Material Design Components for consistent visual design

### Core Dependencies

**Networking and API Communication**
- Retrofit 2 for RESTful API integration with the Go backend server
- Gson converter for JSON serialization and deserialization
- Custom API interfaces defining endpoint contracts and request parameters

**User Interface Libraries**
- Material Design Components providing modern UI patterns and accessibility features
- ConstraintLayout for responsive screen layouts across different device sizes
- Navigation Component for type-safe fragment transitions and deep linking support

**Location and Mapping Services**
- Google Maps SDK for Android providing interactive map displays and location visualization
- Google Play Services Location for accurate GPS positioning and geofencing capabilities
- Custom location management utilities for background location tracking

**Image Processing**
- Glide library for efficient image loading, caching, and transformation
- Base64 encoding utilities for profile image transmission to backend services

**Authentication and Security**
- Firebase Authentication for secure user management and session handling
- Custom security utilities for sensitive data protection

### Development Tools

**IDE Configuration**
- Android Studio with recommended plugins for Java development and Material Design support
- Gradle build configuration with Android plugin version 8.0 and Java 11 compatibility
- Version control integration with Git for collaborative development workflows

## 📋 Feature Implementation

### User Registration and Authentication

The registration process captures comprehensive user information including personal details, location preferences, and service capabilities. The system validates input data and coordinates with Firebase Authentication for secure account creation. Profile setup includes image capture or selection, language preference configuration, and service type selection with customizable assistance categories.

### Intelligent Matching System

The client implements sophisticated filtering algorithms that consider geographic proximity, shared language capabilities, and service type compatibility. Location services provide real-time positioning for accurate distance calculations, while preference matching ensures meaningful connections between volunteers and recipients. The system displays match results through both map-based and list-based interfaces for user convenience.

### Meeting Management Workflow

Meeting creation involves comprehensive validation of volunteer and recipient compatibility, service requirement verification, and scheduling coordination. The client handles meeting state transitions including initial creation, status updates during service delivery, and completion confirmation with optional feedback collection. Real-time status synchronization ensures all parties maintain current information throughout the assistance process.

### Geographic Services Integration

Google Maps integration provides visual context for assistance requests and volunteer locations while respecting privacy preferences. Custom map markers indicate user types and assistance status, with clustering algorithms managing high-density areas. Location tracking operates efficiently with battery optimization and user consent management.

## 🔌 Backend Integration

### API Communication Architecture

The Android client communicates with a Go-based backend server through RESTful API endpoints that provide comprehensive user and meeting management services. The backend implements sophisticated matching algorithms that consider geographic proximity using Haversine distance calculations, language compatibility, and service type alignment.

### Server Endpoints Integration

**User Management APIs**
- User registration and profile creation with comprehensive validation
- Authentication and session management through secure token exchange
- Profile updates and preference modifications with real-time synchronization
- Geographic filtering for nearby user discovery

**Meeting Coordination APIs**
- Meeting creation with automatic conflict detection and resolution
- Status tracking throughout the assistance delivery lifecycle
- Cancellation handling with proper state cleanup and notification
- Historical meeting retrieval for user activity tracking

### Data Synchronization

The client maintains local caching for improved performance while ensuring data consistency through intelligent synchronization strategies. Network connectivity monitoring enables offline capability with automatic synchronization upon connection restoration. Conflict resolution algorithms handle concurrent modifications gracefully.

## 🚀 Installation and Setup

### Development Environment Configuration

Begin by installing Android Studio with the latest stable version and ensuring Android SDK components are current. Configure the development environment with Java 11 support and enable developer options for testing and debugging. Install required SDK packages including Google Play Services and Maps SDK components.

### Project Configuration Steps

Clone the frontend repository and open the project in Android Studio. Configure Gradle sync and resolve any dependency conflicts that may arise. Add required API keys to the project configuration, including Google Maps API key and Firebase configuration files.

**Required Configuration Files**

Create a `gradle.properties` file in the project root with the following configuration:
```properties
MAPS_API_KEY=your_google_maps_api_key
android.useAndroidX=true
android.nonTransitiveRClass=true
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
```

Add the `google-services.json` file to the `app/` directory for Firebase integration. This file contains project-specific configuration for authentication and other Firebase services.

### Build and Deployment Process

Execute Gradle sync to download dependencies and configure build tools. Build the project using Android Studio build tools or command-line Gradle commands. Deploy to physical devices or emulators for testing, ensuring API level 26 or higher for optimal compatibility.

**Backend Server Dependency**

The Android client requires a running backend server for full functionality. The Go server should be accessible at the configured API endpoint, typically `localhost:8080` for development environments. Ensure the backend server is operational before testing client features that require API communication.

## 📱 User Interface Design

### Design System Implementation

The application follows Material Design 3 principles with consistent color schemes, typography, and interaction patterns. Custom themes provide light and dark mode support with accessibility compliance for users with visual impairments. Component libraries ensure visual consistency across different screens and user flows.

### Navigation Architecture

Bottom navigation provides primary access to core application sections including home dashboard, meeting management, and user profile settings. Fragment-based architecture enables smooth transitions and maintains application state during navigation. Deep linking support allows direct access to specific features through external references.

### Responsive Layout Design

Constraint-based layouts adapt seamlessly across different screen sizes and orientations. Custom view components handle edge cases for unusual screen dimensions or accessibility requirements. Touch target sizing follows accessibility guidelines for users with motor impairments.

## 🔧 Development Workflow

### Code Organization

Package structure follows standard Android conventions with clear separation between presentation, domain, and data layers. Naming conventions maintain consistency across the codebase with descriptive class and method names. Documentation standards ensure maintainability for future development efforts.

### Version Control Integration

Git workflow supports feature branch development with pull request reviews for code quality assurance. Commit message standards provide clear change tracking and release management. Continuous integration pipelines automate testing and build verification.

## 📊 Performance Optimization

### Network Efficiency

HTTP request optimization includes connection pooling, request caching, and intelligent retry logic for unreliable network conditions. Image loading optimization reduces bandwidth usage while maintaining visual quality through progressive loading and caching strategies.

### Battery and Resource Management

Location services optimization balances accuracy requirements with battery consumption through intelligent polling intervals and geofencing capabilities. Memory management ensures stable performance across extended usage sessions with proper lifecycle management for Android components.

## 🔐 Security Implementation

### Data Protection

Local data encryption protects sensitive user information stored on device storage. Network communication utilizes HTTPS encryption for all API interactions. Authentication tokens receive secure storage with automatic expiration handling.

### Privacy Compliance

Location data handling follows privacy best practices with explicit user consent and granular permission management. User data minimization ensures only necessary information collection for core functionality. Privacy settings provide user control over data sharing preferences.

## 📈 Future Development

### Planned Enhancements

Push notification integration will provide real-time updates for meeting requests and status changes. Advanced filtering options will enable more precise volunteer-recipient matching based on specific skill sets and availability schedules. Social features may include rating systems and community recognition programs.

### Technical Roadmap

Migration to Jetpack Compose for modern UI development patterns and improved performance characteristics. Integration with additional Google Services for enhanced location accuracy and mapping features. Offline capability expansion for core functionality during network unavailability.

## 👥 Development Team

**Project Contributors**
- Shai Nachum - Android Development and Architecture
- Hadar Barman - UI/UX Design and Implementation

This project represents the culmination of engineering milestone development focusing on practical community assistance solutions through mobile technology innovation.

---

*NeighborGuard Android Client - Empowering communities through accessible technology solutions*
