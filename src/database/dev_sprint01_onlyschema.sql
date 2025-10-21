--
-- PostgreSQL database dump
--

-- Dumped from database version 17.5 (6bc9ef8)
-- Dumped by pg_dump version 17.2

-- Started on 2025-10-21 10:11:47

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- TOC entry 864 (class 1247 OID 49158)
-- Name: authority; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.authority AS ENUM (
    'ROLE_ADMIN',
    'ROLE_USER',
    'ROLE_TEACHER'
);


--
-- TOC entry 867 (class 1247 OID 49166)
-- Name: user_education; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.user_education AS ENUM (
    'HIGH_SCHOOL',
    'COLLEGE',
    'GRAD_SCHOOL',
    'MED_SCHOOL',
    'OTHER'
);


--
-- TOC entry 870 (class 1247 OID 49178)
-- Name: user_hear_app_from; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.user_hear_app_from AS ENUM (
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


--
-- TOC entry 861 (class 1247 OID 49153)
-- Name: user_language; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.user_language AS ENUM (
    'VI',
    'EN'
);


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 218 (class 1259 OID 49214)
-- Name: authorities; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.authorities (
    id bigint NOT NULL,
    email character varying(128) NOT NULL,
    authority public.authority DEFAULT 'ROLE_USER'::public.authority NOT NULL
);


--
-- TOC entry 217 (class 1259 OID 49213)
-- Name: authorities_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.authorities_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 3435 (class 0 OID 0)
-- Dependencies: 217
-- Name: authorities_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.authorities_id_seq OWNED BY public.authorities.id;


--
-- TOC entry 222 (class 1259 OID 90113)
-- Name: google_credentials; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.google_credentials (
    id bigint NOT NULL,
    user_id character varying(255) NOT NULL,
    access_token character varying(1024) NOT NULL,
    refresh_token character varying(1024),
    expires_at timestamp without time zone,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- TOC entry 221 (class 1259 OID 90112)
-- Name: google_credentials_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.google_credentials_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 3436 (class 0 OID 0)
-- Dependencies: 221
-- Name: google_credentials_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.google_credentials_id_seq OWNED BY public.google_credentials.id;


--
-- TOC entry 226 (class 1259 OID 106497)
-- Name: note; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.note (
    id integer NOT NULL,
    note_url character varying(255),
    title character varying(255),
    description text,
    privacy character varying(50),
    status character varying(50),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    id_set integer,
    content text
);


--
-- TOC entry 228 (class 1259 OID 114703)
-- Name: note_docs; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.note_docs (
    id integer NOT NULL,
    file_name text NOT NULL,
    file_url text NOT NULL,
    extension text NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    id_note integer NOT NULL,
    public_id character varying(255)
);


--
-- TOC entry 227 (class 1259 OID 114702)
-- Name: note_docs_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.note_docs_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 3437 (class 0 OID 0)
-- Dependencies: 227
-- Name: note_docs_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.note_docs_id_seq OWNED BY public.note_docs.id;


--
-- TOC entry 225 (class 1259 OID 106496)
-- Name: note_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.note_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 3438 (class 0 OID 0)
-- Dependencies: 225
-- Name: note_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.note_id_seq OWNED BY public.note.id;


--
-- TOC entry 230 (class 1259 OID 139265)
-- Name: note_imgs; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.note_imgs (
    id integer NOT NULL,
    file_name text NOT NULL,
    file_url text NOT NULL,
    extension text NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    id_note integer NOT NULL,
    public_id character varying(255)
);


--
-- TOC entry 229 (class 1259 OID 139264)
-- Name: note_imgs_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.note_imgs_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 3439 (class 0 OID 0)
-- Dependencies: 229
-- Name: note_imgs_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.note_imgs_id_seq OWNED BY public.note_imgs.id;


--
-- TOC entry 232 (class 1259 OID 180243)
-- Name: refresh_token; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.refresh_token (
    id bigint NOT NULL,
    token character varying(255) NOT NULL,
    expiry_date timestamp with time zone NOT NULL,
    user_id bigint NOT NULL,
    revoked boolean DEFAULT false NOT NULL
);


--
-- TOC entry 231 (class 1259 OID 180242)
-- Name: refresh_token_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.refresh_token_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 3440 (class 0 OID 0)
-- Dependencies: 231
-- Name: refresh_token_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.refresh_token_id_seq OWNED BY public.refresh_token.id;


--
-- TOC entry 224 (class 1259 OID 98305)
-- Name: set; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.set (
    id integer NOT NULL,
    id_user integer NOT NULL,
    title character varying(255),
    description text,
    privacy character varying(50),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- TOC entry 223 (class 1259 OID 98304)
-- Name: set_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.set_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 3441 (class 0 OID 0)
-- Dependencies: 223
-- Name: set_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.set_id_seq OWNED BY public.set.id;


--
-- TOC entry 220 (class 1259 OID 57345)
-- Name: users; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.users (
    id integer NOT NULL,
    email character varying(255) NOT NULL,
    first_name character varying(100),
    last_name character varying(100) NOT NULL,
    password character varying(255),
    recovery_code character varying(255),
    language public.user_language DEFAULT 'VI'::public.user_language,
    education public.user_education DEFAULT 'HIGH_SCHOOL'::public.user_education,
    hear_app_from public.user_hear_app_from DEFAULT 'GOOGLE'::public.user_hear_app_from,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    account_type character varying(255) DEFAULT 'FREE'::character varying
);


--
-- TOC entry 219 (class 1259 OID 57344)
-- Name: users_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.users_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 3442 (class 0 OID 0)
-- Dependencies: 219
-- Name: users_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.users_id_seq OWNED BY public.users.id;


--
-- TOC entry 3234 (class 2604 OID 49217)
-- Name: authorities id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.authorities ALTER COLUMN id SET DEFAULT nextval('public.authorities_id_seq'::regclass);


--
-- TOC entry 3243 (class 2604 OID 90116)
-- Name: google_credentials id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.google_credentials ALTER COLUMN id SET DEFAULT nextval('public.google_credentials_id_seq'::regclass);


--
-- TOC entry 3249 (class 2604 OID 106500)
-- Name: note id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.note ALTER COLUMN id SET DEFAULT nextval('public.note_id_seq'::regclass);


--
-- TOC entry 3252 (class 2604 OID 114706)
-- Name: note_docs id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.note_docs ALTER COLUMN id SET DEFAULT nextval('public.note_docs_id_seq'::regclass);


--
-- TOC entry 3255 (class 2604 OID 139268)
-- Name: note_imgs id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.note_imgs ALTER COLUMN id SET DEFAULT nextval('public.note_imgs_id_seq'::regclass);


--
-- TOC entry 3258 (class 2604 OID 180246)
-- Name: refresh_token id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.refresh_token ALTER COLUMN id SET DEFAULT nextval('public.refresh_token_id_seq'::regclass);


--
-- TOC entry 3246 (class 2604 OID 98308)
-- Name: set id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.set ALTER COLUMN id SET DEFAULT nextval('public.set_id_seq'::regclass);


--
-- TOC entry 3236 (class 2604 OID 57348)
-- Name: users id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users ALTER COLUMN id SET DEFAULT nextval('public.users_id_seq'::regclass);


--
-- TOC entry 3261 (class 2606 OID 49220)
-- Name: authorities authorities_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.authorities
    ADD CONSTRAINT authorities_pkey PRIMARY KEY (id);


--
-- TOC entry 3267 (class 2606 OID 90122)
-- Name: google_credentials google_credentials_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.google_credentials
    ADD CONSTRAINT google_credentials_pkey PRIMARY KEY (id);


--
-- TOC entry 3273 (class 2606 OID 114712)
-- Name: note_docs note_docs_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.note_docs
    ADD CONSTRAINT note_docs_pkey PRIMARY KEY (id);


--
-- TOC entry 3275 (class 2606 OID 139274)
-- Name: note_imgs note_imgs_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.note_imgs
    ADD CONSTRAINT note_imgs_pkey PRIMARY KEY (id);


--
-- TOC entry 3271 (class 2606 OID 106506)
-- Name: note note_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.note
    ADD CONSTRAINT note_pkey PRIMARY KEY (id);


--
-- TOC entry 3277 (class 2606 OID 180248)
-- Name: refresh_token refresh_token_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.refresh_token
    ADD CONSTRAINT refresh_token_pkey PRIMARY KEY (id);


--
-- TOC entry 3279 (class 2606 OID 180250)
-- Name: refresh_token refresh_token_token_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.refresh_token
    ADD CONSTRAINT refresh_token_token_key UNIQUE (token);


--
-- TOC entry 3269 (class 2606 OID 98314)
-- Name: set set_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.set
    ADD CONSTRAINT set_pkey PRIMARY KEY (id);


--
-- TOC entry 3263 (class 2606 OID 57359)
-- Name: users users_email_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_email_key UNIQUE (email);


--
-- TOC entry 3265 (class 2606 OID 57357)
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- TOC entry 3282 (class 2606 OID 114713)
-- Name: note_docs fk_note; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.note_docs
    ADD CONSTRAINT fk_note FOREIGN KEY (id_note) REFERENCES public.note(id) ON DELETE CASCADE;


--
-- TOC entry 3283 (class 2606 OID 139275)
-- Name: note_imgs fk_note; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.note_imgs
    ADD CONSTRAINT fk_note FOREIGN KEY (id_note) REFERENCES public.note(id) ON DELETE CASCADE;


--
-- TOC entry 3281 (class 2606 OID 106507)
-- Name: note fk_note_set; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.note
    ADD CONSTRAINT fk_note_set FOREIGN KEY (id_set) REFERENCES public.set(id) ON DELETE CASCADE;


--
-- TOC entry 3280 (class 2606 OID 98315)
-- Name: set fk_user; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.set
    ADD CONSTRAINT fk_user FOREIGN KEY (id_user) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- TOC entry 3284 (class 2606 OID 180251)
-- Name: refresh_token fk_user; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.refresh_token
    ADD CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;


-- Completed on 2025-10-21 10:11:52

--
-- PostgreSQL database dump complete
--

