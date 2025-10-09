CREATE TYPE user_language AS ENUM ('VI', 'EN');

CREATE TYPE authority AS ENUM ('ROLE_ADMIN', 'ROLE_USER', 'ROLE_TEACHER');

CREATE TYPE user_education AS ENUM (
    'HIGH_SCHOOL',
    'COLLEGE',
    'GRAD_SCHOOL',
    'MED_SCHOOL',
    'OTHER'
    );

CREATE TYPE user_hear_app_from AS ENUM (
    'YOUTUBE',
    'TIKTOK',
    'CHATGPT',
    'FACEBOOK',
    'GOOGLE',
    'INSTAGRAM',
    'CLASSMATE',
    'REDDIT',
    'OTHER'
    );

CREATE TABLE "user" (
                        id SERIAL PRIMARY KEY,
                        email VARCHAR(255) UNIQUE NOT NULL,
                        first_name VARCHAR(100) NOT NULL,
                        last_name VARCHAR(100) NOT NULL,
                        password VARCHAR(255) NOT NULL,
                        recovery_code character varying(255) NULL,
                        language user_language default 'VI',

                        education user_education default 'HIGH_SCHOOL',

                        hear_app_from user_hear_app_from default 'GOOGLE',

                        created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE
    public.authorities (
                           id bigserial primary key NOT NULL,
                           email character varying(128) NOT NULL,
                           authority authority NOT NULL DEFAULT 'ROLE_USER'
);









