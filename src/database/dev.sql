CREATE TABLE user (
                      id SERIAL PRIMARY KEY,
                      email VARCHAR(255) UNIQUE NOT NULL,
                      first_name VARCHAR(100) NOT NULL,
                      last_name VARCHAR(100) NOT NULL,
                      password VARCHAR(255) NOT NULL,
                      language VARCHAR(50) DEFAULT 'vi'
                          CHECK (language IN ('vi', 'en')),

    education VARCHAR(100)
        CHECK (education IN (
            'High School',
            'College',
            'Grad School',
            'Med School',
            'Other'
        )),

    role VARCHAR(50) NOT NULL
        CHECK (role IN ('Admin', 'Teacher', 'Student')),

    hear_app_from VARCHAR(255)
        CHECK (hear_app_from IN (
            'YouTube',
            'TikTok',
            'ChatGPT',
            'Facebook',
            'Google',
            'Instagram',
            'Classmate',
            'Reddit',
            'Other'
        )),

    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);