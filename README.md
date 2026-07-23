

https://github.com/user-attachments/assets/89bb3edc-15dd-4f0f-99ea-38a63c40c129

# ValuePick!!!!

가치투자 지표 기반 종목 스크리닝 서비스. DART 재무제표, 공공데이터포털 주가, KRX 지수, 한국수출입은행 환율을 매일 자동 수집해 PER·PBR·ROE·Piotroski F-Score 등 투자지표를 계산하고, 다팩터 가중 스코어링으로 저평가 우량주 TOP100을 추천합니다. 개인 투자일지(매수/매도 기록, 손익 관리) 기능을 함께 제공합니다.

- 서비스 주소: https://www.valuepick.cloud
- 배포 상태: 1차 배포 완료

## 목차

- [주요 기능](#주요-기능)
- [기술 스택](#기술-스택)
- [폴더 구조](#폴더-구조)
- [데이터 파이프라인](#데이터-파이프라인)
- [API 개요](#api-개요)
- [실행 방법](#실행-방법)
- [1차 배포 범위](#1차-배포-범위)
- [2차 로드맵](#2차-로드맵)

## 주요 기능

### 종목 스크리닝
- 전체 종목 목록 조회 · PER/PBR/ROE/배당수익률 범위 필터 · 정렬 · 페이징 · 종목명 검색
- 종목 상세: 재무제표(손익계산서·재무상태표) 연도별 추이, 투자지표, 관련 뉴스, Canvas 차트
- 홈 화면: 코스피 지수·환율 위젯, TOP10 추천 종목, 4대 랭킹(저PER/저PBR/고ROE/고배당) — 10초 주기 실시간 시세 갱신
- TOP100 랭킹: Piotroski F-Score + 7개 팩터 가중 스코어링 결과 페이지

### 투자지표 스코어링 엔진
- **투자지표 계산**: EPS·BPS·PER·PBR·ROE·ROA·부채비율·배당수익률·모멘텀(12-1)·EPS성장률, 외화 재무제표는 환율 환산 후 계산
- **Piotroski F-Score(0~9점)**: 수익성 4 + 재무건전성 3 + 운영효율성 2 항목, 금융업은 구조상 제외 처리
- **TOP100 스코어**: F-Score 6점 이상 통과 종목만 후보로, PER 25% · ROE 20% · PBR·부채비율 15% · ROA·모멘텀 10% · EPS성장률 5% 가중치로 백분위 정규화 후 합산

### 회원/인증
- JWT(Access/Refresh) 기반 회원가입·로그인·토큰 재발급
- 마이페이지: 닉네임/비밀번호 변경, 관심종목 관리, 회원탈퇴(soft delete, 30일 유예 후 스케줄러가 완전 삭제)
- 관심종목 등록/해제

### 투자일지
- 종목별 매수/매도 기록, 보유/완료 상태 관리, 메모, 제목 수정, 공유 여부 토글
- 카테고리·기간·검색 조건 페이징 조회

## 기술 스택

**Backend**
- Java 21, Spring Boot 3.5.15
- Spring Security + JWT(jjwt), Spring Data JPA(Hibernate), MySQL 8
- Jsoup(뉴스 크롤링), RestTemplate(외부 API 연동)
- JUnit5

**Frontend**
- HTML / CSS / Vanilla JavaScript (Multi-Page Application)

**Infra**
- Docker / Docker Compose
- Nginx (정적 파일 서빙 + 리버스 프록시 + HTTPS)
- AWS EC2, Let's Encrypt
- Jenkins (CI/CD)

## 폴더 구조

```
invest-project/
├── valuepick/                  # Spring Boot 백엔드
│   └── src/main/java/com/example/demo/
│       ├── config/             # Security, JWT, CORS, WebMvc 설정
│       ├── domain/
│       │   ├── controller/     # REST 컨트롤러
│       │   ├── service/        # 비즈니스 로직 (지표계산, 스코어링, 수집기 등)
│       │   ├── entity/         # JPA 엔티티
│       │   ├── repository/     # Spring Data JPA 리포지토리
│       │   ├── dto/            # 요청/응답 DTO
│       │   └── scheduled/      # 배치 스케줄러
│       └── global/exception/   # 전역 예외 처리
├── front/                       # 정적 프론트엔드 (MPA)
│   ├── *.html                  # 페이지별 HTML
│   ├── js/                     # 페이지별 JS
│   ├── css/                    # 페이지별 CSS
│   └── nginx.conf              # 운영용 Nginx 설정
├── DB/                          # MySQL Dockerfile
├── JENKINS/                     # Jenkins 이미지 설정
├── docker-compose.yml           # 운영 배포 구성
└── docker-compose-local.yml     # 로컬 개발 구성
```

## 데이터 파이프라인

매일 새벽(Asia/Seoul 기준) 배치가 순차 실행되어 최신 데이터를 반영합니다.

| 시각 | 스케줄러 | 작업 |
|---|---|---|
| 01:00 (평일) | ExchangeScheduler | 환율 수집 |
| 01:10 (평일) | MarketIndexScheduler | 코스피 지수 수집 |
| 01:20 (평일) | StockPriceScheduler | 주가 수집 |
| 01:50 (평일) | IndicatorScheduler | 투자지표 계산 (PER/PBR/ROE/F-Score/모멘텀 등) |
| 02:00 (평일) | Top100Scheduler | TOP100 스코어 계산 |
| 02:30~02:35 | 각 스케줄러 | 7일 이전 시세/지수/환율/TOP100 데이터 정리 |
| 매시 정각 | NewsScheduler | 종목별 뉴스 수집 |
| 매년 1월 1일 | CompanyScheduler | 기업정보(DART) 수집 |
| 매년 4월 1일 | FinancialScheduler / DividendScheduler | 사업보고서·배당 정보 수집 |
| 매일 자정 | UserCleanupScheduler | 탈퇴 30일 경과 유저 완전 삭제 |

## API 개요

| 경로 | 설명 | 로그인 필요 여부 |
|---|---|---|
| `POST /api/auth/register`, `/login`, `/refresh` | 회원가입 · 로그인 · 토큰 재발급 | 불필요 (로그인 이전 단계이므로) |
| `GET /info/**` | 종목 목록/필터/검색/랭킹/TOP10·100/지수/환율 조회 | 불필요 |
| `GET /api/stocks/**` | 종목 상세/뉴스/검색/재무제표 조회 | 불필요 |
| `/api/favorites/**` | 관심종목 등록/조회/해제 | **필요** |
| `/api/journal/**` | 투자일지 CRUD | **필요** |
| `/api/users/me/**` | 마이페이지 (내 정보, 닉네임/비밀번호 변경, 탈퇴) | **필요** |
| `/admin/**`, `/company/**` | 데이터 수집 트리거 등 관리자 작업 | **필요 (ADMIN 권한)** |

## 실행 방법

### 로컬 (Docker Compose)

```bash
# valuepick/.env, DB/.env 준비 (DB 계정, JWT_SECRET, 외부 API 키 등)
docker compose -f docker-compose-local.yml up --build
```

- 프론트: http://localhost:3000
- 백엔드: http://localhost:8080
- DB: localhost:3330

### 운영 (AWS EC2)

```bash
docker compose up --build -d
```

- `fn`(Nginx)만 80/443 포트를 외부에 노출, `bn`(Spring)·`db`(MySQL)는 내부 네트워크에서만 통신
- HTTPS는 Let's Encrypt 인증서 사용, HTTP 요청은 HTTPS로 리다이렉트

## 1차 배포 범위

**포함**
- 종목 스크리닝, 투자지표/TOP100 스코어링 엔진, 데이터 자동 수집 파이프라인
- JWT 인증, 마이페이지, 관심종목, 개인 투자일지
- Docker 기반 배포, HTTPS, CI/CD(Jenkins)

**미포함 (코드는 존재하나 화면 비노출)**
- 커뮤니티 게시판(투자일지 공유), 관리자 페이지 UI — 메뉴에서만 숨김 처리, 관련 API는 서버 권한 검사로 보호됨

## 2차 로드맵

- 프론트엔드 React 전환
- 관리자 페이지 활성화
- 커뮤니티 게시판(투자일지 공유) 및 댓글 기능
- Redis 도입 (세션/캐시)
- 소셜 로그인(OAuth2) 연동
