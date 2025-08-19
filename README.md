## 🚀 Key Features

- **OAuth 2.0 with PKCE**: Secure authentication flow following OAuth 2.0 standards
- **Chrome Custom Tabs**: Uses Chrome Custom Tabs instead of WebView
- **Secure Token Storage**: EncryptedSharedPreferences with Android Keystore
- **Automatic Sign-in**: Silent sign-in when valid token exists
- **User Profile Display**: Shows user name, email, and profile picture
- **Efficient Image Loading**: Uses Coil with fallback handling
- **Secure Logout**: Clears tokens and user data
- **Clean Architecture**: MVVM with Jetpack Compose
- **Error Handling**: Graceful handling for all scenarios

## 🏗️ Project Architecture

### Clean Architecture Pattern

```
┌─────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                   │
├─────────────────────────────────────────────────────────┤
│  MainActivity  │  SignInScreen  │  ProfileScreen        │
│  SignInViewModel │  ProfileViewModel                     │
└─────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────┐
│                     DOMAIN LAYER                        │
├─────────────────────────────────────────────────────────┤
│  AuthManager  │  AuthRepository (Interface)             │
└─────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────┐
│                      DATA LAYER                         │
├─────────────────────────────────────────────────────────┤
│  AuthRepositoryImpl  │  OAuthService  │  TokenStorage   │
│  OAuthConfig  │  PKCEUtil  │  Models                    │
└─────────────────────────────────────────────────────────┘
```

### Data Flow

```
UI Screen → ViewModel → Repository → OAuthService → Google API
                              ↓
                        TokenStorage ← ← ← ← ← ← ← ← ← ← ←
```

### Package Structure

```
com.example.lavegatest/
├── auth/                          # Authentication logic
│   ├── AuthManager.kt            # OAuth callback coordinator
│   ├── OAuthConfig.kt            # OAuth constants & config
│   ├── OAuthService.kt           # OAuth flow implementation
│   ├── OAuthState.kt             # OAuth state management
│   ├── PKCEUtil.kt               # PKCE utilities
│   ├── TokenStorage.kt           # Secure token storage
│   └── model/                    # Data models
│       ├── TokenResponse.kt      # OAuth token response
│       ├── UserProfile.kt        # User profile data
│       └── OAuthError.kt         # OAuth error handling
├── data/
│   └── repository/
│       └── AuthRepository.kt     # Repository pattern
├── navigation/
│   ├── LavegaNavHost.kt          # Navigation setup
│   └── NavRoutes.kt              # Route definitions
├── presentation/
│   ├── auth/
│   │   ├── SignInScreen.kt       # Sign-in UI
│   │   └── SignInViewModel.kt    # Sign-in logic
│   └── profile/
│       ├── ProfileScreen.kt      # Profile UI
│       └── ProfileViewModel.kt   # Profile logic
└── MainActivity.kt               # App entry point
```

## 🔍 Key Components

### AuthRepository
Central data access layer:
- Interface and implementation pattern
- Manages authentication state with StateFlow
- Handles OAuth flow and token storage
- Provides clean API for ViewModels

### SignInViewModel & ProfileViewModel
Separate ViewModels for each screen:
- **SignInViewModel**: Manages sign-in process and OAuth callbacks
- **ProfileViewModel**: Manages user profile display and sign-out
- Clean separation of concerns
- Easy to test and maintain

### AuthManager
Coordinates OAuth callbacks:
- Bridges MainActivity and ViewModels
- Manages OAuth callback flow
- Provides repository access

### OAuthService
Handles complete OAuth 2.0 flow:
- Builds authorization URLs with PKCE
- Launches Chrome Custom Tabs
- Exchanges authorization codes for tokens
- Fetches user profile information

### TokenStorage
Secure storage using EncryptedSharedPreferences:
- Stores access tokens, refresh tokens, and user profiles
- Automatic encryption/decryption
- Token expiry management

## 🔄 OAuth Flow

### 1. Authorization Request
- Generates PKCE code verifier and challenge
- Builds authorization URL with required parameters
- Launches Chrome Custom Tabs

### 2. User Authentication
- User authenticates with Google
- Google redirects to app with authorization code

### 3. Token Exchange
- App exchanges authorization code for tokens
- Stores tokens securely using EncryptedSharedPreferences

### 4. Profile Fetching
- Fetches user profile using Google People API
- Displays user information in Profile screen

## 📱 UI Components

### SignInScreen
- Clean Material Design 3 interface
- Google Sign-In button
- Loading states and error handling
- Responsive layout

### ProfileScreen
- User profile display with avatar
- Name, email, and additional information
- Sign-out functionality
- Error state handling


## 🔐 Google Client ID Setup ------------

### Step 1: Google Cloud Console

1. Visit [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select existing project
3. Enable required APIs:
   - Google+ API
   - Google People API

### Step 2: OAuth Consent Screen

1. Go to **APIs & Services** → **OAuth consent screen**
2. Select **External** user type
3. Fill in information:
   ```
   App name: Lavega Android Test
   User support email: your-email@gmail.com
   Developer contact information: your-email@gmail.com
   ```
4. Add scopes:
   - `openid`
   - `email`
   - `profile`

### Step 3: Create OAuth 2.0 Client ID

1. Go to **APIs & Services** → **Credentials**
2. Click **Create Credentials** → **OAuth 2.0 Client IDs**
3. Select **Android** application type
4. Fill in information:
   ```
   Package name: com.example.lavegatest
   SHA-1 certificate fingerprint: [your-sha1-fingerprint]
   ```

### Step 4: Get SHA-1 Fingerprint

**Debug Certificate:**
```bash
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
```

**Release Certificate:**
```bash
keytool -list -v -keystore your-release-key.keystore -alias your-key-alias
```

### Step 5: Configure Client ID

1. Open `local.properties` file
2. Add Google Client ID:
   ```properties
   google_client_id=YOUR_CLIENT_ID.apps.googleusercontent.com
   ```