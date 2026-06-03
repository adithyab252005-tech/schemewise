import { createContext, useContext, useState, useEffect } from 'react';
import axios from 'axios';

// ---------------- ZERO KNOWLEDGE CRYPTO CORE ----------------
const str2buf = (str) => new TextEncoder().encode(str);
const buf2str = (buf) => new TextDecoder().decode(buf);
const buf2hex = (buf) => Array.from(new Uint8Array(buf)).map(b => b.toString(16).padStart(2, '0')).join('');
const hex2buf = (hex) => new Uint8Array(hex.match(/.{1,2}/g).map(byte => parseInt(byte, 16))).buffer;

const deriveKey = async (password, emailSalt) => {
    const keyMaterial = await crypto.subtle.importKey(
        'raw', str2buf(password), { name: 'PBKDF2' }, false, ['deriveKey']
    );
    const key = await crypto.subtle.deriveKey(
        { name: 'PBKDF2', salt: str2buf(emailSalt + '_schemewise'), iterations: 100000, hash: 'SHA-256' },
        keyMaterial, { name: 'AES-GCM', length: 256 }, true, ['encrypt', 'decrypt']
    );
    const rawKey = await crypto.subtle.exportKey('raw', key);
    return buf2hex(rawKey);
};

const getImportedKey = async (hexKey) => {
    return await crypto.subtle.importKey(
        'raw', hex2buf(hexKey), { name: 'AES-GCM' }, false, ['encrypt', 'decrypt']
    );
};

const encryptData = async (text, hexKey) => {
    if (!text) return text;
    try {
        const key = await getImportedKey(hexKey);
        const iv = crypto.getRandomValues(new Uint8Array(12));
        const encrypted = await crypto.subtle.encrypt({ name: 'AES-GCM', iv }, key, str2buf(text));
        return buf2hex(iv.buffer) + ':' + buf2hex(encrypted);
    } catch(e) { console.error('Encrypt failed', e); return text; }
};

const decryptData = async (cipherText, hexKey) => {
    if (!cipherText || !cipherText.includes(':')) return cipherText; // Not encrypted
    try {
        const [ivHex, dataHex] = cipherText.split(':');
        const key = await getImportedKey(hexKey);
        const decrypted = await crypto.subtle.decrypt(
            { name: 'AES-GCM', iv: new Uint8Array(hex2buf(ivHex)) },
            key, hex2buf(dataHex)
        );
        return buf2str(decrypted);
    } catch(e) { console.error('Decrypt failed - Data Unreadable', e); return '***ENCRYPTED***'; }
};
// -----------------------------------------------------------

const ProfileContext = createContext();

const API_BASE = 'http://180.235.121.245:8060/api/v1';

export const ProfileProvider = ({ children }) => {
    // Start empty, we will load from DB
    const [profiles, setProfiles] = useState([]);

    const [activeProfileId, setActiveProfileId] = useState(() => {
        return sessionStorage.getItem('schemewise_active_profile_id') || null;
    });

    const [isAuthenticated, setIsAuthenticated] = useState(() => {
        return sessionStorage.getItem('schemewise_auth') === 'true';
    });

    const [role, setRole] = useState(() => {
        return sessionStorage.getItem('schemewise_role') || 'user';
    });

    // Maintain what email logged in for the DB lookup
    const [userEmail, setUserEmail] = useState(() => {
        return sessionStorage.getItem('schemewise_email') || '';
    });

    useEffect(() => {
        if (activeProfileId) {
            sessionStorage.setItem('schemewise_active_profile_id', activeProfileId);
        } else {
            sessionStorage.removeItem('schemewise_active_profile_id');
        }
    }, [activeProfileId]);

    useEffect(() => {
        sessionStorage.setItem('schemewise_auth', isAuthenticated);
        sessionStorage.setItem('schemewise_role', role);
        sessionStorage.setItem('schemewise_email', userEmail);
    }, [isAuthenticated, role, userEmail]);

    // Load profile from DB when authenticated
    useEffect(() => {
        const loadProfileFromDB = async () => {
            if (isAuthenticated && userEmail) {
                try {
                    const res = await axios.get(`${API_BASE}/users/${userEmail}`);
                    if (res.data && res.data.user) {
                        const dbProfile = { ...res.data.user, id: res.data.user.id.toString() };
                        setProfiles([dbProfile]);
                        setActiveProfileId(dbProfile.id);
                    }
                } catch (error) {
                    console.log("Could not load profile from DB. Might be a new user.", error);
                }
            }
        };

        loadProfileFromDB();
    }, [isAuthenticated, role, userEmail]);

    const addProfile = async (profileData) => {
        const sessionKey = sessionStorage.getItem('schemewise_zk_key');
        let dataToSave = { ...profileData, email: userEmail || profileData.email };
        
        // Zero-Knowledge Encrypt Category
        if (sessionKey && dataToSave.category) {
            dataToSave.category = await encryptData(dataToSave.category, sessionKey);
        }

        try {
            // FIX: If userEmail exists, this is an update to an existing verified account, NOT a new registration.
            const endpoint = userEmail ? `${API_BASE}/users/${userEmail}` : `${API_BASE}/users/`;
            const res = await axios.post(endpoint, dataToSave);
            if (res.data && res.data.user) {
                const newProfile = { ...res.data.user, id: res.data.user.id.toString() };
                setProfiles([newProfile]);
                setActiveProfileId(newProfile.id);
                return newProfile;
            }
        } catch (error) {
            console.error("Failed to save profile to DB:", error);
            // Fallback to local
            const newProfile = { ...dataToSave, id: Date.now().toString() };
            setProfiles([newProfile]);
            setActiveProfileId(newProfile.id);
            return newProfile;
        }
    };

    const updateProfile = async (id, updatedData) => {
        const existingProfile = profiles.find(p => p.id === id);
        const sessionKey = sessionStorage.getItem('schemewise_zk_key');
        let dataToSave = { ...existingProfile, ...updatedData, email: userEmail || updatedData.email };

        // Zero-Knowledge Encrypt Category
        if (sessionKey && updatedData.category) {
            dataToSave.category = await encryptData(updatedData.category, sessionKey);
        }

        try {
            const res = await axios.put(`${API_BASE}/users/${dataToSave.email}`, dataToSave);
            if (res.data && res.data.user) {
                const updatedDbProfile = { ...res.data.user, id: res.data.user.id.toString() };
                setProfiles(prev => prev.map(p => p.id === id ? updatedDbProfile : p));
                return updatedDbProfile;
            }
        } catch (error) {
            console.error("Failed to update profile in DB:", error);
            // Fallback to local
            const newProfile = { ...existingProfile, ...updatedData };
            setProfiles(prev => prev.map(p => p.id === id ? newProfile : p));
            return newProfile;
        }
    };

    const deleteProfile = async (id) => {
        setProfiles(prev => prev.filter(p => p.id !== id));
        if (activeProfileId === id) {
            setActiveProfileId(null);
        }
    };

    const [decryptedProfile, setDecryptedProfile] = useState(null);

    // Asynchronously decrypt active profile
    useEffect(() => {
        const decryptActive = async () => {
            const active = profiles.find(p => p.id === activeProfileId) || null;
            if (!active) { setDecryptedProfile(null); return; }
            
            const sessionKey = sessionStorage.getItem('schemewise_zk_key');
            if (sessionKey && active.category && active.category.includes(':')) {
                const decCat = await decryptData(active.category, sessionKey);
                setDecryptedProfile({ ...active, category: decCat });
            } else {
                setDecryptedProfile(active);
            }
        };
        decryptActive();
    }, [activeProfileId, profiles]);

    const getActiveProfile = () => {
        return decryptedProfile;
    };

    const value = {
        profiles,
        activeProfileId,
        setActiveProfileId,
        addProfile,
        updateProfile,
        deleteProfile,
        getActiveProfile,
        isAuthenticated,
        role,
        userEmail,
        setUserEmail,
        login: async (userRole = 'user', email = '', userData = null, password = '') => {
            setIsAuthenticated(true);
            setRole(userRole);
            if (email) setUserEmail(email);
            
            // Derive ZK Session Key if password is provided
            if (password && email) {
                const hexKey = await deriveKey(password, email);
                sessionStorage.setItem('schemewise_zk_key', hexKey);
            }

            if (userData) {
                const dbProfile = { ...userData, id: userData.id.toString() };
                setProfiles([dbProfile]);
                setActiveProfileId(dbProfile.id);
            }
        },
        logout: () => {
            setIsAuthenticated(false);
            setRole('user');
            setUserEmail('');
            setProfiles([]);
            setActiveProfileId(null);
            sessionStorage.removeItem('schemewise_zk_key');
        }
    };

    return (
        <ProfileContext.Provider value={value}>
            {children}
        </ProfileContext.Provider>
    );
};

export const useProfile = () => {
    const context = useContext(ProfileContext);
    if (!context) {
        throw new Error('useProfile must be used within a ProfileProvider');
    }
    return context;
};
