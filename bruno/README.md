# MediBook API - Bruno Test Collection

This folder contains Bruno API test collections for testing the MediBook API endpoints individually.

## Prerequisites

1. Install Bruno API client from [https://www.usebruno.com/](https://www.usebruno.com/)
2. Ensure your MediBook API backend is running on `http://localhost:8080` (or configure the appropriate environment)

## Folder Structure

```
bruno/
├── bruno.json                      # Collection configuration
├── environments/                   # Environment configurations
│   ├── Local.bru                  # Local development environment
│   ├── Development.bru            # Development server environment
│   └── Production.bru             # Production environment
├── auth/                          # Authentication endpoints
│   ├── register-patient.bru
│   ├── register-doctor.bru
│   ├── register-admin.bru
│   ├── signin.bru
│   ├── signin-pending-doctor-fail.bru
│   ├── signin-approved-doctor-success.bru
│   ├── signout.bru
│   ├── refresh-token.bru
│   └── ... (error test cases)
├── admin/                         # Admin endpoints
│   ├── get-pending-doctors.bru
│   ├── approve-doctor.bru
│   ├── reject-doctor.bru
│   └── admin-access-unauthorized.bru
└── turns/                         # Turn management endpoints
    ├── create-turn.bru
    ├── get-available-turns.bru
    ├── reserve-turn.bru
    ├── get-my-turns.bru
    └── ... (additional turn operations)
```

## How to Use

### 1. Open Collection in Bruno
1. Open Bruno
2. Click "Open Collection"
3. Navigate to this `bruno` folder and select it

### 2. Select Environment
1. In Bruno, select the appropriate environment from the dropdown (Local, Development, or Production)
2. The Local environment is configured for `http://localhost:8080`

### 3. Test Flow

#### Authentication Flow:
1. **Register Users**: Start by registering a patient, doctor, and/or admin using the register endpoints
2. **Doctor Approval Flow**: 
   - Register a doctor (status will be PENDING)
   - Admin approves the doctor (changes status to ACTIVE)
   - Doctor can now sign in
3. **Sign In**: Use the `signin.bru` endpoint to authenticate and get tokens
4. **Test Protected Endpoints**: The signin endpoint automatically saves tokens to environment variables

#### Admin Workflow:
1. **View Pending Doctors**: Use `/api/admin/pending-doctors` to see doctors awaiting approval
2. **Approve Doctor**: Use `/api/admin/approve-doctor/{id}` to approve a pending doctor
3. **Reject Doctor**: Use `/api/admin/reject-doctor/{id}` to reject a pending doctor

#### Turn Management Flow:
1. **Get Available Turns**: Check available time slots for a doctor
2. **Create Turn**: Create a new turn appointment
3. **Reserve Turn**: Reserve an available turn as a patient
4. **Get My Turns**: View turns for the authenticated user

### 4. Environment Variables

The following variables are automatically managed:
- `accessToken`: JWT access token (set after signin)
- `refreshToken`: Refresh token (set after signin)
- `userId`: Current user ID
- `userRole`: Current user role (PATIENT, DOCTOR, ADMIN)
- `doctorId`: Doctor ID for testing
- `patientId`: Patient ID for testing
- `turnId`: Turn ID for testing
- `pendingDoctorId`: Doctor ID saved after registration (for admin approval)
- `doctorAccessToken`: Doctor's access token (for testing non-admin access)

### 5. Running Tests

Each `.bru` file contains:
- **Request configuration**: HTTP method, URL, headers, body
- **Assertions**: Expected response status and structure
- **Tests**: JavaScript test cases for validation
- **Scripts**: Post-response scripts for saving tokens/IDs

## API Endpoints Covered

### Authentication (`/api/auth`)
- `POST /register/patient` - Register a new patient
- `POST /register/doctor` - Register a new doctor (status: PENDING)
- `POST /register/admin` - Register a new admin
- `POST /signin` - Sign in and get tokens (only ACTIVE users)
- `POST /signout` - Sign out and invalidate tokens
- `POST /refresh-token` - Refresh access token

### Admin Management (`/api/admin`)
- `GET /pending-doctors` - Get all doctors with PENDING status
- `POST /approve-doctor/{id}` - Approve a pending doctor (PENDING → ACTIVE)
- `POST /reject-doctor/{id}` - Reject a pending doctor (PENDING → REJECTED)

### Turn Management (`/api/turns`)
- `POST /` - Create a new turn
- `GET /available` - Get available time slots
- `POST /reserve` - Reserve a turn
- `GET /my-turns` - Get current user's turns
- `GET /doctor/{doctorId}` - Get turns for a specific doctor
- `GET /patient/{patientId}` - Get turns for a specific patient

## Error Test Cases

The collection includes test cases for common error scenarios:
- Invalid email format
- Short password
- Invalid credentials
- Unauthorized access
- Forbidden operations

## Tips

1. **Run tests in sequence**: Some tests depend on previous ones (e.g., signin before protected endpoints)
2. **Check environment variables**: Ensure tokens and IDs are properly set after authentication
3. **Update test data**: Modify email addresses and other test data to avoid conflicts
4. **Monitor assertions**: Each test includes assertions to verify correct behavior
5. **Use filters**: Use status parameter in turn endpoints to filter results

## Troubleshooting

- **401 Unauthorized**: Ensure you've signed in and the access token is set
- **403 Forbidden**: Check that the user role has permission for the operation
- **400 Bad Request**: Verify request body format and required fields
- **Database errors**: Ensure PostgreSQL is running and configured correctly