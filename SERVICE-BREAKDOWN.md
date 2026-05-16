# MediConnect Microservices - Team Breakdown
## Exactly What Each Team Member Builds (Spring Boot + MongoDB)

---

# 👤 ZAKARIA - MS-01, MS-02, MS-09

## Service 1: MS-01 Authentication & Identity

### MongoDB Collections (Create in MongoDB):
```
users
sessions
mfa_settings
tokens
login_attempts
certificates
```

### Spring Boot Components to Build:

**Repository Layer** (Spring Data MongoDB):
```
UserRepository extends MongoRepository
SessionRepository extends MongoRepository
MFASettingsRepository extends MongoRepository
TokenRepository extends MongoRepository
LoginAttemptRepository extends MongoRepository
CertificateRepository extends MongoRepository
```

**Service Layer** (Dependency Injection with @Service, @Autowired):
```
AuthenticationService
  - login(email, password)
  - logout(userId)
  - refreshToken(token)
  - validateToken(token)

MFAService
  - setupTOTP(userId) → returns QR code
  - enableTOTP(userId, secret, code)
  - sendSMSCode(userId) → integrate Twilio
  - verifyMFACode(userId, code)

SessionService
  - getUserSessions(userId)
  - revokeSession(sessionId)
  - logoutAllDevices(userId)

JwtTokenProvider
  - generateAccessToken(user) → 15 min expiry
  - generateRefreshToken(user) → 7 day expiry
  - extractUserId(token)
  - validateToken(token)
```

**Controller Layer** (REST Endpoints with DI):
```
AuthController
  POST   /api/auth/login
  POST   /api/auth/logout
  POST   /api/auth/refresh
  POST   /api/auth/mfa/setup
  POST   /api/auth/mfa/verify
  GET    /api/auth/sessions
  DELETE /api/auth/sessions/{sessionId}
  POST   /api/auth/password/reset-request
  POST   /api/auth/password/reset
```

**Configuration Classes**:
```
SecurityConfig
  - Configure Spring Security
  - JWT authentication filter
  - Password encoder (BCrypt)

KafkaConfig
  - Configure Kafka producer
```

### Kafka Events to Publish:
```
user.login
user.logout
auth.failed
mfa.required
mfa.verified
```

### External Integrations:
```
Twilio (SMS for MFA)
Email service (password reset)
```

### Key Features:
✅ Email/password login
✅ MFA (TOTP, SMS)
✅ JWT tokens (access + refresh)
✅ Session management
✅ Account lockout (5 failed attempts)
✅ Rate limiting (5 login attempts per 15 min)
✅ Moroccan professional card/license verification (doctor/pharmacist)
✅ National ID + registration number proof documents

---

## Service 2: MS-02 User Management

### MongoDB Collections:
```
patient_profiles
doctor_profiles
pharmacist_profiles
subscriptions
bulk_imports
clinic_accounts
```

### Spring Boot Components:

**Repository Layer**:
```
PatientProfileRepository extends MongoRepository
DoctorProfileRepository extends MongoRepository
PharmacistProfileRepository extends MongoRepository
SubscriptionRepository extends MongoRepository
BulkImportRepository extends MongoRepository
ClinicAccountRepository extends MongoRepository
```

**Service Layer**:
```
UserProfileService
  - createPatientProfile(userId, request)
  - updatePatientProfile(userId, request)
  - getPatientProfile(userId)
  - createDoctorProfile(userId, request)
  - searchDoctors(specialty, language, location)
  - createPharmacistProfile(userId, request)

SubscriptionService
  - createSubscription(userId, planType)
  - upgradePlan(userId, newPlanType)
  - downgradePlan(userId, newPlanType)
  - cancelSubscription(userId)
  - (Integrates with Stripe for payment)

BulkImportService
  - processBulkImport(userId, csvFile)
  - parseCSV(file)
  - validateUser(csvRow)
  - createUser(csvRow)
  - sendInvitationEmail(email, tempPassword)

ClinicAccountService
  - createClinicAccount(name, siretNumber)
  - inviteTeamMember(clinicId, userEmail)
  - getClinicsByUser(userId)
```

**Controller Layer**:
```
UserManagementController
  POST   /api/users/patients
  GET    /api/users/patients/{userId}
  PUT    /api/users/patients/{userId}
  POST   /api/users/doctors
  GET    /api/users/doctors/{userId}
  GET    /api/users/doctors/search?specialty=Cardiology&language=French
  POST   /api/users/pharmacists
  GET    /api/users/pharmacists/{userId}
  PUT    /api/users/doctors/{userId}/verification (admin)
  PUT    /api/users/pharmacists/{userId}/verification (admin)
  PUT    /api/users/{userId}/subscription
  POST   /api/users/batch-import (CSV upload)
  GET    /api/users/batch-import/{importId}/status
  GET    /api/users/search?specialty=Cardiology&city=Paris
```

### Kafka Events to Publish:
```
user.created
user.updated
user.suspended
user.deleted
subscription.upgraded
subscription.downgraded
```

### External Integrations:
```
Stripe (payment processing)
Email service (invitations, notifications)
```

### Key Features:
✅ Patient profiles (DOB, blood type, insurance, allergies)
✅ Doctor profiles (Moroccan registration number + CIN + specialty)
✅ Pharmacist profiles (Moroccan registration number + CIN + hours, delivery)
✅ Subscription plans (BASIC, PREMIUM, ENTERPRISE)
✅ Bulk CSV import with validation
✅ Doctor search (specialty, language, location)
✅ Plan-based feature limits (max patients, max appointments)

---

## Service 3: MS-09 Audit & Compliance

### MongoDB Collections:
```
audit_logs (IMMUTABLE - append-only)
gdpr_requests
anomaly_flags
access_logs
prescription_audit
consent_logs
data_retention_policy
```

### Spring Boot Components:

**Repository Layer**:
```
AuditLogRepository extends MongoRepository
GDPRRequestRepository extends MongoRepository
AnomalyFlagRepository extends MongoRepository
AccessLogRepository extends MongoRepository
```

**Service Layer**:
```
AuditLogService
  - logAction(userId, action, resourceId, timestamp)
  - getAuditLogs(filters) → searchable by user/date/action
  - logDataAccess(userId, patientId, sections, reason)
  - (Uses append-only pattern - no updates)

GDPRService
  - requestDataExport(userId) → generates ZIP
  - requestDeletion(userId) → 30-day grace period
  - completeDataDeletion(userId) → irreversible
  - exportAsJSON/PDF/FHIR

AnomalyDetectionService
  - detectSuspiciousLogin(userId, ipAddress) → flag if unusual
  - detectBulkDataAccess(userId) → flag if accessing 100+ patients
  - detectOffHoursAccess(userId) → flag if accessing at 2 AM
  - detectMultipleFailedLogins(userId) → flag if >5 failures

ComplianceReportService
  - generateMonthlyReport() → GDPR/HDS status
  - generateAnnualReport()
  - trackDataRetention()
```

**Controller Layer**:
```
AuditController
  GET    /api/audit/logs
  GET    /api/audit/logs/user/{userId}
  GET    /api/audit/logs/patient/{patientId}
  GET    /api/audit/access-trail/prescription/{prescriptionId}
  GET    /api/gdpr/export/{userId}
  POST   /api/gdpr/export-request
  DELETE /api/gdpr/forget/{userId}
  GET    /api/compliance/status
  GET    /api/anomalies
```

### Kafka Events to Consume (Listen):
```
(All events from other services)
user.login → log
user.logout → log
user.created → log
dmp.updated → log
dmp.accessed → log
prescription.created → log
prescription.signed → log
prescription.dispensed → log
appointment.booked → log
teleconsult.started → log
message.sent → log
```

### Kafka Events to Publish:
```
audit.report.generated
anomaly.detected
gdpr.export.ready
gdpr.deletion.completed
```

### Key Features:
✅ Immutable audit logs (append-only, no updates)
✅ Complete activity tracking (who did what when)
✅ GDPR data export (JSON, PDF, HL7 FHIR)
✅ Right-to-be-forgotten (30-day grace period)
✅ Anomaly detection (suspicious logins, bulk access, unusual times)
✅ Access audit trail (who accessed patient records)
✅ Monthly/annual compliance reports
✅ Data retention policy enforcement

---

---

# 👨‍⚕️ DALAL - MS-03, MS-04

## Service 1: MS-03 Medical Records (DMP)

### MongoDB Collections:
```
dmp_records
allergies
medications
chronic_conditions
consultations
lab_results
imaging_results
vaccinations
procedures
consent_records
health_notebook_entries
documents
access_logs
```

### Spring Boot Components:

**Repository Layer**:
```
DMPRepository extends MongoRepository
AllergyRepository extends MongoRepository
MedicationRepository extends MongoRepository
ConditionRepository extends MongoRepository
ConsultationRepository extends MongoRepository
LabResultRepository extends MongoRepository
ImagingRepository extends MongoRepository
VaccinationRepository extends MongoRepository
ConsentRepository extends MongoRepository
AccessLogRepository extends MongoRepository
DocumentRepository extends MongoRepository
```

**Service Layer**:
```
DMPService
  - createDMP(patientId) → auto-create for new patients
  - getDMP(patientId) → full record
  - getDMP(patientId, sections) → filtered (allergies only, etc.)
  - updateDMP(patientId, data)

AllergyService
  - addAllergy(patientId, allergen, severity, reaction)
  - getAllergies(patientId)
  - deleteAllergy(patientId, allergyId)
  - checkAllergy(patientId, allergen) → true/false

MedicationService
  - addMedication(patientId, drug, dosage, frequency)
  - getCurrentMedications(patientId)
  - updateMedication(medicationId, changes)
  - removeMedication(medicationId)
  - trackRefills(patientId)

ConditionService
  - addCondition(patientId, diagnosis, icd10Code)
  - getConditions(patientId)
  - updateCondition(conditionId, status)

ConsultationService
  - addConsultation(patientId, doctorId, findings, assessment)
  - getConsultationHistory(patientId) → timeline

LabResultService
  - addLabResult(patientId, testName, values, reportPDF)
  - getLabResults(patientId)
  - parseLabReport(pdf) → OCR extraction

ImagingService
  - addImagingStudy(patientId, type, dicomPath, interpretation)
  - getImagingResults(patientId)

VaccinationService
  - addVaccination(patientId, vaccine, date, lotNumber)
  - getVaccinations(patientId)
  - getReminderForNextDose(patientId)

HealthNotebookService
  - logVitals(patientId, bp, glucose, weight, heartRate)
  - getTrends(patientId, metric, days) → returns graph data
  - getAlerts(patientId) → BP too high, glucose too low

ConsentService
  - grantAccess(patientId, doctorId, sections, expiryDate)
  - revokeAccess(patientId, doctorId)
  - getConsents(patientId) → who has access
  - verifyAccess(patientId, doctorId) → check permission

AccessLogService
  - logAccess(patientId, accessedBy, sections, reason)
  - getAccessHistory(patientId)

DocumentService
  - uploadDocument(patientId, file)
  - categorizeDocument(file) → Lab, Imaging, Prescription, etc.
  - scanForViruses(file)
```

**Controller Layer**:
```
DMPController
  GET    /api/dmp/{patientId}
  GET    /api/dmp/{patientId}?include=allergies,medications
  POST   /api/dmp/{patientId}/documents
  GET    /api/dmp/{patientId}/documents
  DELETE /api/dmp/{patientId}/documents/{docId}
  GET    /api/dmp/{patientId}/allergies
  POST   /api/dmp/{patientId}/allergies
  DELETE /api/dmp/{patientId}/allergies/{allergyId}
  GET    /api/dmp/{patientId}/medications
  POST   /api/dmp/{patientId}/medications
  PUT    /api/dmp/{patientId}/medications/{medId}
  GET    /api/dmp/{patientId}/conditions
  GET    /api/dmp/{patientId}/consultations
  GET    /api/dmp/{patientId}/lab-results
  GET    /api/dmp/{patientId}/imaging
  PUT    /api/dmp/{patientId}/consent
  GET    /api/dmp/{patientId}/consent
  GET    /api/dmp/{patientId}/access-log
  POST   /api/dmp/{patientId}/health-notebook
  GET    /api/dmp/{patientId}/health-notebook/trends?metric=BP&days=30
  GET    /api/dmp/{patientId}/alerts
  POST   /api/dmp/export-fhir/{patientId}
```

### Kafka Events to Consume:
```
user.created → auto-create DMP
prescription.dispensed → update medications list
```

### Kafka Events to Publish:
```
dmp.updated
dmp.accessed
allergy.alert
```

### External Integrations:
```
DICOM viewer (medical imaging)
HL7 FHIR R4 (export to national DMP)
OCR technology (parse lab reports)
SNOMED CT database (medical codes)
```

### Key Features:
✅ Complete patient medical record
✅ Allergies with severity levels (LIFE_THREATENING, SEVERE, MODERATE, MILD)
✅ Current medications with refill tracking
✅ Chronic conditions (ICD-10 coded)
✅ Consultation history (timeline)
✅ Lab results with reference ranges
✅ Imaging studies with DICOM support
✅ Vaccination records
✅ Patient health notebook (self-logged vitals)
✅ Consent management (granular, time-limited, revocable)
✅ Access audit trail (who accessed what, when)
✅ HL7 FHIR R4 export (interoperability)

---

## Service 2: MS-04 Prescription Management

### MongoDB Collections:
```
prescriptions
prescription_items
interactions_checked
allergies_checked
signatures
qr_codes
dispensations
refill_history
prescription_status_history
controlled_substance_log
```

### Spring Boot Components:

**Repository Layer**:
```
PrescriptionRepository extends MongoRepository
PrescriptionItemRepository extends MongoRepository
InteractionRepository extends MongoRepository
SignatureRepository extends MongoRepository
QRCodeRepository extends MongoRepository
DispensationRepository extends MongoRepository
RefillHistoryRepository extends MongoRepository
```

**Service Layer**:
```
PrescriptionService
  - createPrescription(patientId, doctorId, items[])
  - getPrescription(prescriptionId)
  - editPrescription(prescriptionId, changes) → only if DRAFT
  - deletePrescription(prescriptionId) → only if DRAFT
  - getPrescriptionsByPatient(patientId)
  - getPrescriptionsByDoctor(doctorId)
  - getStatus(prescriptionId) → DRAFT, SIGNED, SENT, DISPENSED

DrugInteractionService
  - checkInteractions(newDrug, currentMedications) → from Vidal API
  - getInteractionDetails(drug1, drug2) → severity, mechanism, recommendation
  - searchAlternatives(drug) → similar drugs without interactions
  - checkContraindications(drug, age, kidneyFunction) → safe?
  - checkPregnancyCategory(drug) → Category A/B/C/D/X
  - calculateDosageAdjustment(drug, age, weight, kidneyFunction)

AllergyCheckService
  - checkAllergy(patientId, drug) → flag if allergic
  - checkSimilarDrugs(drug) → flag if patient allergic to similar class
  - returnAllergyDetails(patientId, drug) → severity, reaction

DigitalSignatureService
  - signPrescription(prescriptionId, doctorId, eIDASCertificate)
  - verifySignature(prescriptionId) → valid/invalid
  - validateCertificate(certificate) → expired? revoked?
  - checkCRL/OCSP(certificate) → is certificate revoked?

QRCodeService
  - generateQRCode(prescriptionId) → encoded data
  - encodeQRData(prescriptionId, signature, timestamp) → encrypted
  - scanAndValidateQR(qrCodeData) → returns prescription ID

PharmacyTransmissionService
  - sendToPhamacy(prescriptionId, pharmacyId)
  - trackTransmission(prescriptionId) → delivery status
  - retryTransmission(prescriptionId) → if failed

DispensationService
  - dispenseMedication(prescriptionId, pharmacyId, pharmacistId)
  - recordDispensation(prescriptionId, batchNumber, quantity, date)
  - handlePartialDispensation(prescriptionId) → some meds available, some not
  - updateDMP(patientId) → add to patient's medication list

RefillService
  - requestRefill(prescriptionId, patientId)
  - approveRefill(prescriptionId, doctorId) → doctor reviews
  - autoRefill(prescriptionId) → for chronic medications
  - checkRefillsRemaining(prescriptionId)
  - createNewPrescription(prescriptionId) → from refill

ExpiryService
  - checkExpiry(prescriptionId) → valid/expired
  - markExpired(prescriptionId) → after 1 year
  - preventDispensing(prescriptionId) → if expired
```

**Controller Layer**:
```
PrescriptionController
  POST   /api/prescriptions
  GET    /api/prescriptions/{id}
  PUT    /api/prescriptions/{id}
  DELETE /api/prescriptions/{id}
  POST   /api/prescriptions/{id}/sign
  POST   /api/prescriptions/{id}/send-to-pharmacy
  POST   /api/prescriptions/{id}/dispense
  GET    /api/prescriptions/{id}/qr
  GET    /api/prescriptions/{id}/interactions
  POST   /api/prescriptions/{id}/refill-request
  POST   /api/prescriptions/{id}/refill-approve
  POST   /api/prescriptions/check-interactions (before creating)
  GET    /api/prescriptions/patient/{patientId}
  GET    /api/prescriptions/doctor/{doctorId}
  GET    /api/prescriptions/pharmacy/{pharmacyId}
```

### Kafka Events to Consume:
```
dmp.updated → check new allergies/medications
```

### Kafka Events to Publish:
```
prescription.created
prescription.signed
prescription.sent
prescription.dispensed
drug.interaction.alert
prescription.expired
prescription.refilled
```

### External Integrations:
```
Vidal API (French drug database + interactions)
eIDAS provider (digital signatures)
Pharmacy systems (transmission)
Email/SMS service (notifications)
```

### Key Features:
✅ Prescription creation with drug selection
✅ Real-time drug interaction checking (Vidal)
✅ Allergy cross-checking
✅ Digital signature (eIDAS, non-repudiation)
✅ QR code generation (encrypted)
✅ Transmission to pharmacy
✅ Dispensation workflow
✅ Refills (manual + automatic for chronic)
✅ Prescription expiry tracking (1 year)
✅ Controlled substance handling
✅ Status tracking (DRAFT → SIGNED → DISPENSED)

---

---

# 📅 DOUAE - MS-05, MS-06

## Service 1: MS-05 Appointment & Scheduling

### MongoDB Collections:
```
doctor_schedules
appointments
appointment_slots
no_shows
wait_list
check_ins
appointment_feedback
appointment_history
```

### Spring Boot Components:

**Repository Layer**:
```
DoctorScheduleRepository extends MongoRepository
AppointmentRepository extends MongoRepository
AppointmentSlotRepository extends MongoRepository
NoShowRepository extends MongoRepository
WaitListRepository extends MongoRepository
CheckInRepository extends MongoRepository
AppointmentFeedbackRepository extends MongoRepository
```

**Service Layer**:
```
ScheduleService
  - setupDoctorSchedule(doctorId, workHours, lunchBreak, appointmentDuration)
  - getSchedule(doctorId)
  - updateSchedule(doctorId, changes)
  - addVacationPeriod(doctorId, startDate, endDate)
  - removeVacationPeriod(doctorId, vacationId)

AvailabilityService
  - getAvailableSlots(doctorId, date) → uses Redis cache
  - calculateSlots(doctorId, date) → free time - booked - lunch
  - cacheSlots(doctorId, slots) → Redis TTL 60 min
  - invalidateCache(doctorId) → when new booking
  - findNextAvailableSlot(doctorId) → for patient searching

AppointmentService
  - bookAppointment(patientId, doctorId, dateTime, type)
  - getAppointment(appointmentId)
  - rescheduleAppointment(appointmentId, newDateTime)
  - cancelAppointment(appointmentId, reason)
  - getPatientAppointments(patientId)
  - getDoctorAppointments(doctorId)
  - checkDoubleBooking(doctorId, dateTime) → prevent conflicts

ReminderService
  - send4DayReminder(appointmentId) → email
  - send1DayReminder(appointmentId) → SMS
  - send1HourReminder(appointmentId) → SMS urgent
  - send15MinReminder(appointmentId) → in-app notification

CheckInService
  - checkInPatient(appointmentId) → "I'm here"
  - getWaitingRoomPosition(appointmentId) → "#3 in line"
  - estimateWaitTime(appointmentId) → "15 minutes"
  - updateQueuePosition() → real-time

NoShowService
  - markNoShow(appointmentId)
  - logNoShow(appointmentId, patientId, reason)
  - getNoShowRate(doctorId) → percentage
  - getNoShowRate(patientId) → percentage

WaitListService
  - addToWaitList(patientId, doctorId, requestedDate)
  - getWaitListPosition(patientId, doctorId)
  - removeFromWaitList(patientId, doctorId)
  - notifyWhenSlotAvailable(patientId) → SMS/email

FeedbackService
  - submitFeedback(appointmentId, rating, comments)
  - getAverageRating(doctorId)
  - getPatientSatisfaction(doctorId)

StatisticsService
  - getDoctorStats(doctorId) → total appointments, no-show rate, avg duration
  - getClinicStats(clinicId) → total visits, busiest hours, revenue
```

**Controller Layer**:
```
AppointmentController
  GET    /api/agenda/{doctorId}/slots?date=2026-05-10
  GET    /api/agenda/{doctorId}/schedule
  POST   /api/agenda/{doctorId}/vacation
  DELETE /api/agenda/{doctorId}/vacation/{vacationId}
  GET    /api/doctors/search?specialty=Cardiology
  GET    /api/doctors/{doctorId}/availability
  POST   /api/appointments
  GET    /api/appointments/{id}
  PUT    /api/appointments/{id}
  DELETE /api/appointments/{id}
  GET    /api/appointments/patient/{patientId}
  GET    /api/appointments/doctor/{doctorId}
  POST   /api/appointments/{id}/check-in
  GET    /api/appointments/{id}/queue-position
  POST   /api/appointments/{id}/no-show
  POST   /api/appointments/{id}/feedback
  GET    /api/wait-list/{patientId}
  POST   /api/wait-list
```

### Kafka Events to Publish:
```
appointment.booked
appointment.cancelled
appointment.rescheduled
appointment.no_show
```

### External Integrations:
```
Redis (cache availability slots)
Twilio (SMS reminders)
Email service (appointment confirmations)
Calendar APIs (Google Calendar, Outlook sync)
```

### Key Features:
✅ Doctor schedule setup (work hours, lunch, vacation)
✅ Real-time appointment availability (Redis cached)
✅ Slot-based booking (prevent double-booking)
✅ Auto-reminders (24h, 1h, 15min before)
✅ Reschedule/cancel with reason tracking
✅ Virtual waiting room (show position in queue)
✅ No-show tracking & statistics
✅ Wait list management
✅ Doctor/patient performance statistics
✅ Multi-location support

---

## Service 2: MS-06 Teleconsultation Video Service

### MongoDB Collections:
```
video_sessions
session_participants
waiting_room_queue
recordings
session_chat
session_events
session_quality_metrics
screen_shares
```

### Spring Boot Components:

**Repository Layer**:
```
VideoSessionRepository extends MongoRepository
SessionParticipantRepository extends MongoRepository
WaitingRoomRepository extends MongoRepository
RecordingRepository extends MongoRepository
SessionChatRepository extends MongoRepository
SessionEventRepository extends MongoRepository
```

**Service Layer**:
```
VideoSessionService
  - createSession(appointmentId) → generates session ID
  - generateJoinLink(sessionId, userRole) → doctor/patient-specific link
  - startSession(sessionId) → doctor initiates
  - getSessionStatus(sessionId) → WAITING, ACTIVE, ENDED

WaitingRoomService
  - addToWaitingRoom(sessionId, patientId) → patient joins before doctor
  - getQueuePosition(patientId) → "#3 in queue"
  - admitFromWaitingRoom(sessionId, patientId) → doctor admits patient
  - estimateWaitTime(sessionId) → calculated from current consultation

ParticipantService
  - addParticipant(sessionId, userId, role) → doctor/patient
  - removeParticipant(sessionId, userId)
  - trackJoinTime(sessionId, userId)
  - trackLeaveTime(sessionId, userId)

EncryptionService (DTLS-SRTP)
  - generateSessionKey() → unique per session
  - initiateDTLSHandshake(sessionId) → key exchange
  - encryptMediaStream(data) → AES-256
  - decryptMediaStream(data)
  - validateSignatures() → perfect forward secrecy

WebRTCSignalingService
  - handleSDPOffer(sessionId, sdpData) → answer
  - handleICECandidate(sessionId, iceCandidate)
  - negotiate(sessionId) → P2P connection setup
  - handleConnectionFailure(sessionId) → reconnect logic

QualityMonitoringService
  - trackBitrate(sessionId) → Kbps
  - trackLatency(sessionId) → milliseconds
  - trackPacketLoss(sessionId) → percentage
  - adjustQuality(sessionId) → adaptive bitrate
  - switchQuality(sessionId, newResolution) → 720p → 480p → 360p

RecordingService
  - startRecording(sessionId) → with consent flag
  - stopRecording(sessionId)
  - validateConsent(sessionId) → check agreement
  - storeRecording(sessionId, videoFile) → encrypted MongoDB
  - trackRecordingDuration(sessionId)
  - deleteRecordingAfterRetention(sessionId) → 2 year expiry

ChatService
  - sendMessage(sessionId, senderId, message) → in-call chat
  - getChat(sessionId) → all messages in session
  - storeChat(sessionId, messages)

ScreenShareService
  - startScreenShare(sessionId, doctorId)
  - stopScreenShare(sessionId)
  - trackScreenShareDuration(sessionId)
  - shareImage(sessionId, image) → medical images
  - validateScreenShare() → doctor only

NetworkRecoveryService
  - detectConnectionLoss(sessionId) → after 5 sec
  - triggerAutoReconnect(sessionId) → attempt 5 times
  - offlineQueue(sessionId) → buffer data during outage
  - resumeSession(sessionId) → after reconnect
  - fallbackToPhone(sessionId) → switch to phone call

SessionManagementService
  - endSession(sessionId) → graceful close
  - forceEndSession(sessionId) → admin force-close
  - calculateSessionDuration(sessionId)
  - generateSessionSummary(sessionId)
  - trackSessionMetrics(sessionId)
```

**Controller Layer**:
```
TeleconsultController
  POST   /api/teleconsult/sessions
  GET    /api/teleconsult/sessions/{id}
  POST   /api/teleconsult/sessions/{id}/start
  GET    /api/teleconsult/sessions/{id}/join
  POST   /api/teleconsult/sessions/{id}/admit-next
  POST   /api/teleconsult/sessions/{id}/end
  GET    /api/teleconsult/sessions/{id}/status
  POST   /api/teleconsult/sessions/{id}/share-screen
  POST   /api/teleconsult/sessions/{id}/record-start
  POST   /api/teleconsult/sessions/{id}/record-stop
  GET    /api/teleconsult/sessions/{id}/chat
  POST   /api/teleconsult/sessions/{id}/chat/message
  GET    /api/teleconsult/wait-queue/{doctorId}
  GET    /api/teleconsult/sessions/{id}/recording
```

### Kafka Events to Consume:
```
appointment.booked → pre-allocate session
```

### Kafka Events to Publish:
```
teleconsult.started
teleconsult.ended
teleconsult.recording_started
teleconsult.recording_ended
```

### External Integrations:
```
Janus Gateway (WebRTC media server)
TURN/STUN servers (NAT traversal)
Twilio (fallback to phone)
Email service (session summaries)
```

### Key Features:
✅ WebRTC video calling (peer-to-peer)
✅ End-to-end encryption (DTLS-SRTP)
✅ Virtual waiting room (queue management)
✅ Screen sharing (doctor shares medical images)
✅ Recording with consent
✅ In-call chat
✅ Adaptive bitrate (auto-adjust to network)
✅ Auto-reconnect (5 attempts, then fallback to phone)
✅ Session timeout (30 min inactive)
✅ Quality metrics tracking (bitrate, latency, packet loss)
✅ Perfect forward secrecy (old calls unreadable if key stolen)

---

---

# 💬 DINA - MS-07, MS-08

## Service 1: MS-07 Secure Messaging

### MongoDB Collections:
```
conversations
messages
message_attachments
message_reactions
encryption_keys
read_receipts
archived_conversations
message_audit_log
```

### Spring Boot Components:

**Repository Layer**:
```
ConversationRepository extends MongoRepository
MessageRepository extends MongoRepository
AttachmentRepository extends MongoRepository
ReactionRepository extends MongoRepository
EncryptionKeyRepository extends MongoRepository
ReadReceiptRepository extends MongoRepository
```

**Service Layer**:
```
ConversationService
  - createConversation(participants[]) → 1-1 or multi-party
  - getConversations(userId) → all conversations
  - openConversation(conversationId) → fetch messages
  - archiveConversation(conversationId)
  - unarchiveConversation(conversationId)
  - muteConversation(conversationId)
  - pinConversation(conversationId)

MessageService
  - sendMessage(conversationId, senderId, content)
  - getMessage(messageId)
  - deleteMessage(messageId) → after 1 hour only
  - editMessage(messageId, newContent) → within 15 min
  - getMessages(conversationId, limit, offset) → pagination
  - searchMessages(conversationId, query) → full-text search

EncryptionService (Signal Protocol)
  - initializeConversation(conversationId) → key exchange
  - generateKeyPair() → ECDH
  - exchangeKeys(user1, user2) → Diffie-Hellman
  - encryptMessage(messageId, content) → AES-256-GCM
  - decryptMessage(messageId) → only recipient can read
  - rotateKeys() → perfect forward secrecy
  - verifySignature(messageId) → authenticity

AttachmentService
  - uploadAttachment(conversationId, messageId, file)
  - encryptAttachment(file) → before storage
  - storeAttachment(file) → MongoDB or cloud
  - downloadAttachment(attachmentId) → decrypt on client
  - scanForVirus(file) → antivirus check
  - deleteAttachment(attachmentId)

ReadReceiptService
  - markAsRead(messageId, userId)
  - getReadReceipts(messageId) → who read it
  - trackReadTime(messageId) → timestamp
  - showTypingIndicator(conversationId, userId) → "is typing..."

ReactionService
  - addReaction(messageId, userId, emoji)
  - removeReaction(messageId, userId, emoji)
  - getReactions(messageId) → all reactions

AccessControlService
  - verifyAccess(userId, conversationId) → participant check
  - checkMessagePermission(userId, messageId) → can read?
  - restrictAccessAfterExpiry(conversationId, expiryDate)

ComplianceService
  - logMessage(messageId) → for audit
  - getMessageAuditLog(conversationId) → who accessed
  - enforceRetention(conversationId) → delete after 2 years
  - handleLegalHold(conversationId) → preserve forever
```

**Controller Layer**:
```
MessagingController
  GET    /api/messages/conversations
  POST   /api/messages/conversations
  GET    /api/messages/conversations/{id}
  POST   /api/messages/conversations/{id}/messages
  PUT    /api/messages/{id}/read
  DELETE /api/messages/{id}
  PUT    /api/messages/{id}/edit
  POST   /api/messages/{id}/react
  POST   /api/messages/conversations/{id}/attachments
  GET    /api/messages/conversations/{id}/attachments
  GET    /api/messages/search?query=diabetes
  PUT    /api/messages/conversations/{id}/archive
  PUT    /api/messages/conversations/{id}/mute
  PUT    /api/messages/conversations/{id}/pin
```

### Kafka Events to Publish:
```
message.sent
message.read
message.delivered
```

### External Integrations:
```
Signal Protocol library (encryption)
File storage (MongoDB or S3)
Email service (notifications)
Antivirus API (file scanning)
```

### Key Features:
✅ End-to-end encryption (Signal Protocol - same as WhatsApp)
✅ Perfect forward secrecy (old messages unreadable if key stolen)
✅ One-on-one & multi-party conversations
✅ File attachments (encrypted storage)
✅ Reactions (emoji)
✅ Read receipts & typing indicators
✅ Full-text search on encrypted messages
✅ Message archival (hide old conversations)
✅ Message deletion (within 1 hour)
✅ Legal hold (preserve for litigation)
✅ Conversation muting & pinning
✅ Compliance audit logging

---

## Service 2: MS-08 Notification Service

### MongoDB Collections:
```
notification_queue
notification_history
user_preferences
notification_templates
sms_delivery_logs
email_delivery_logs
push_delivery_logs
failed_notifications
```

### Spring Boot Components:

**Repository Layer**:
```
NotificationQueueRepository extends MongoRepository
NotificationHistoryRepository extends MongoRepository
UserPreferencesRepository extends MongoRepository
TemplateRepository extends MongoRepository
DeliveryLogRepository extends MongoRepository
```

**Kafka Consumer** (Listen to all events):
```
KafkaConsumerService
  - Listen to: user.login, user.logout
  - Listen to: appointment.booked, appointment.cancelled, appointment.started
  - Listen to: prescription.created, prescription.dispensed
  - Listen to: drug.interaction.alert, allergy.alert
  - Listen to: message.sent, message.read
  - Listen to: teleconsult.started
  - Listen to: user.suspended, password.changed
  - Listen to: all events from other services
```

**Service Layer**:
```
NotificationService
  - publishNotification(userId, type, content)
  - sendNotification(userId, message, channels[])
  - selectChannels(userId, notificationType) → SMS/Email/Push/In-app

SMSService
  - sendSMS(phoneNumber, message) → Twilio
  - trackDelivery(smsId) → status
  - retryOnFailure(smsId) → 3 attempts
  - logSMSEvent(smsId, status, error)

EmailService
  - sendEmail(email, subject, body, attachments)
  - trackDelivery(emailId)
  - handleBounce(emailId) → remove invalid email
  - retryOnFailure(emailId)

PushNotificationService
  - sendPush(deviceToken, title, message)
  - trackDelivery(pushId)
  - handleTokenExpiry(userId, deviceId) → refresh token

InAppNotificationService
  - storeNotification(userId, notification)
  - getNotifications(userId) → list
  - markAsRead(notificationId)
  - deleteNotification(notificationId)

TemplateService
  - getTemplate(eventType) → SMS/Email/Push templates
  - renderTemplate(template, variables) → fill placeholders
  - createTemplate(type, template)
  - updateTemplate(templateId, newContent)

PreferencesService
  - getUserPreferences(userId)
  - setChannelPreference(userId, channel, enabled)
  - setDoNotDisturbHours(userId, startTime, endTime)
  - setNotificationFrequency(userId, immediate/daily/weekly)
  - optIn(userId, notificationType)
  - optOut(userId, notificationType)

DeduplicationService
  - checkIfSent(userId, eventType, resourceId, timeWindow=5min)
  - isDuplicate(notification) → true/false
  - preventSpam(userId) → max 5 SMS per day

RateLimitService
  - enforceRateLimit(userId, channel) → max notifications
  - calculateNextAllowed(userId) → timestamp
  - prioritizeUrgent(notification) → CRITICAL bypasses limits

QueueManagementService
  - enqueueNotification(notification) → Redis queue
  - processQueue() → batch send
  - retryFailed(notificationId) → exponential backoff
  - deadLetterQueue(notificationId) → after 3 failures

EventConsumerService
  - onAppointmentBooked(event)
    → Queue SMS: 4d before, 1d before, 1h before
  - onPrescriptionCreated(event)
    → Queue SMS to patient + Email
  - onDrugInteractionAlert(event)
    → URGENT SMS to doctor + pharmacist
  - onMessageSent(event)
    → Push to recipient
  - onTeleconsultStarted(event)
    → URGENT notification "Starting NOW"
  
AnalyticsService
  - trackDeliveryRate(channel) → % successfully delivered
  - trackOptOutRate() → % unsubscribed
  - trackEngagementRate() → % who opened
  - getBestTimeToSend() → analyze patterns
  - getChannelEffectiveness() → which channels work best
```

**Controller Layer**:
```
NotificationController
  GET    /api/notifications/user/{userId}
  GET    /api/notifications/user/{userId}/unread
  PUT    /api/notifications/{id}/read
  DELETE /api/notifications/{id}
  GET    /api/notifications/preferences/{userId}
  PUT    /api/notifications/preferences/{userId}
  POST   /api/notifications/opt-in/{notificationType}
  POST   /api/notifications/opt-out/{notificationType}
  GET    /api/notifications/delivery-stats
  POST   /api/notifications/send (admin manual send)
```

### Kafka Events to Consume (All):
```
user.login / user.logout
appointment.booked / appointment.cancelled / appointment.started
prescription.created / prescription.dispensed
drug.interaction.alert / allergy.alert
message.sent / message.read
teleconsult.started / teleconsult.ended
lab_result.ready
user.suspended
password.changed
(and more...)
```

### External Integrations:
```
Twilio (SMS)
SendGrid or AWS SES (Email)
Firebase Cloud Messaging (Push)
Redis (queue management)
Kafka (event consumption)
```

### Key Features:
✅ Multi-channel delivery (SMS, Email, Push, In-app)
✅ Smart channel selection (urgent → SMS, details → Email)
✅ Appointment reminders (4d, 1d, 1h, 15min before)
✅ Prescription alerts (ready for pickup, dispensed)
✅ Drug interaction alerts (CRITICAL URGENT)
✅ Message notifications
✅ User preference control (opt-in/out)
✅ Do Not Disturb settings (quiet hours)
✅ Deduplication (prevent duplicate alerts)
✅ Rate limiting (max 5 SMS/day unless urgent)
✅ Retry logic (3 attempts with exponential backoff)
✅ Delivery tracking (sent, delivered, read)
✅ Queue management (persistent, handle failures)
✅ Analytics (delivery rate, opt-out rate, best times)

---

## 📌 KEY ARCHITECTURE POINTS (Remember!)

✅ **Dependency Injection (DI)**
- All services use @Service annotation
- All injections use @Autowired
- Controllers inject services
- Services inject repositories

✅ **Inversion of Control (IoC)**
- Spring container manages all beans
- Configuration classes provide beans (@Configuration, @Bean)
- Lifecycle automatically managed

✅ **Spring Data MongoDB (JPA Pattern)**
- Entities use @Document annotation (not @Entity)
- Repositories extend MongoRepository
- CRUD methods auto-implemented
- Custom queries with @Query annotation

✅ **Kafka Event-Driven** ----------------------------->>>> hadi htalkher nbghiw nlasqo services -ila kan darory sf diriha
- Services publish events (KafkaTemplate)
- Consumers listen to events (@KafkaListener)
- Asynchronous, decoupled services
- 16+ event types flowing through system

✅ **Security**
- Spring Security for authentication
- JWT tokens for stateless sessions
- Password encoding with BCrypt
- Method-level authorization (@PreAuthorize)

---

**DABA hadxi li hna khaso ykon fi conception li andiro lyoma inshaallah o ltahte atl9aw exmpl dyal hade project .** 🚀
EXMPLE :
  Reality with MediConnect:

   9:00 AM: Maria wakes up, sore throat
   9:05 AM: Opens app, searches "doctors near me"
            Sees 5 doctors available TODAY
            Clicks: "Dr. Paul - Video call - Available in 1 hour"

   10:00 AM: Video call with Dr. Paul (15 min)
             Dr. Paul: "You have strep throat"
             Writes prescription in app (DIGITALLY SIGNED)

   10:15 AM: SMS arrives: "Your prescription ready at Pharmacy Leclerc"

   1:00 PM: Maria walks to pharmacy
            Shows QR code on phone
            Pharmacist: "Here's your medicine"
            Maria leaves with antibiotics

   ✓ Total time: 30 minutes
   ✓ Total money spent: €0 (insurance pays)
   ✓ Total stress: Zero

   

  ----------------------------------------------------------------------------------------------------------------------------------------------------

