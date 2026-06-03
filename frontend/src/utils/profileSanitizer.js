/**
 * sanitizeProfileForEvaluation
 * 
 * Converts a raw frontend profile object (from ProfileContext / DB)
 * into the clean payload expected by the backend /evaluate endpoint.
 *
 * Handles:
 *  - ZK-encrypted category (AES-GCM hex blobs like "iv:ciphertext")
 *  - "Prefer not to say" category (user privacy choice)
 *  - Snake_case to camelCase field name mapping
 *  - Null / undefined guards
 */
export function sanitizeProfileForEvaluation(profile) {
    if (!profile) return {};

    const rawCat = (profile.category || '').trim();
    const isPrivateOrEncrypted = (
        !rawCat ||
        rawCat.toLowerCase() === 'prefer not to say' ||
        rawCat.includes(':')   // ZK-encrypted AES-GCM hex blob
    );

    return {
        state:              profile.state || '',
        income:             profile.income || 0,
        // Empty string tells backend: "unknown, treat gracefully"
        category:           isPrivateOrEncrypted ? '' : rawCat,
        occupation:         profile.occupation || '',
        // Support both snake_case (DB) and camelCase (legacy)
        ruralUrban:         profile.rural_urban || profile.ruralUrban || 'Urban',
        age:                profile.age || '',
        gender:             profile.gender || '',
        isBPL:              profile.is_bpl || profile.isBPL || 'No',
        isStudent:          profile.is_student || profile.isStudent || 'No',
        isDifferentlyAbled: profile.is_differently_abled || profile.isDifferentlyAbled || 'No',
        maritalStatus:      profile.marital_status || profile.maritalStatus || 'Single',
        isFarmer:           profile.is_farmer || profile.isFarmer || 'No',
        employmentType:     profile.employment_type || profile.employmentType || '',
        // Student detail fields
        studentLevel:       profile.student_level || profile.studentLevel || '',
        studentClass:       profile.student_class || profile.studentClass || '',
        studentDegreeType:  profile.student_degree_type || profile.studentDegreeType || '',
        studentCourse:      profile.student_course || profile.studentCourse || '',
    };
}
