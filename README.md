![image](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2Fs2pI8%2FbtsKvC8F6fw%2FMYmpzfE0viiwGk9AyFrGU0%2Fimg.png)

# TICKET🎫SPRING
공연 등 다양한 문화 및 엔터테이먼트 이벤트의 티켓을 온라인으로 예매할 수 있는 서비스

# 🔥 How to build
### [Java SpringBoot Swagger 적용하기](https://everyday-spring.com/625)

- Java SpringBoot Swagger 적용하기
- Swagger GET Method RequestBody
- Swagger Request Form

### [지랄 아니고요 지라입니다 Jira로 프로젝트 관리하기](https://everyday-spring.com/626)

- 프로젝트 관리 툴 Jira 도입
- Jira - GitHub 연동하기
  
![project_blue_2024-11-05_07 56pm](https://github.com/user-attachments/assets/f19aec83-e838-4d49-8126-853e80b92654)

### Window PC에 Docker, jenkins 설치, GitHub 연동하기

- [ (1) 난 대학시절 삽질을 전공했단 사실](https://everyday-spring.com/628)
- [ (2) 난 삽질로 유학까지 다녀왔단 사실](https://everyday-spring.com/629)

### AWS EC2에 Docker Jenkins 설치, Github Spring Boot application CI/CD

- [ (1) CI/CD를 위한 EC2 준비하기](https://everyday-spring.com/630)
- [ (2) EC2 Server 연결하기, ubuntu 메모리 스왑](https://everyday-spring.com/631)
- [ (3) Docker, Jenkins 설치 | Docker in Docker](https://subin0522.tistory.com/632)
- [ (4) Jenkins Pipeline CI/CD](https://subin0522.tistory.com/633)

### [EC2 ubuntu Jenkins CI/CD 트러블 슈팅](https://everyday-spring.com/634)

- Jenkins Spring Boot build 무한로딩
- Spring Boot Spotless Plugin build resource
- Docker not found
- 그런데 또 무한로딩이 걸림 미쳐버리겠네
- not in a git directory
- Jenkins CI/CD 환경변수

### AOP 를 사용하여 알림설정
- [ (1) spring 과 slack 알림 연동 - 1](https://k-chongchong.tistory.com/40)
- [ (2) AOP를 사용한 알림 연동 - 2 ](https://k-chongchong.tistory.com/41)
- [ (3) 이벤트 기반 아키텍처(EDA)와 AOP의 비교 -3 ](https://k-chongchong.tistory.com/42)

### elastic 환경설정 및 검색 api구현과정
- https://velog.io/@uara67/Spring-ELK-1-엘라스틱-서치-그게-뭔데
- https://velog.io/@uara67/Spring-ELK-Docker-Elastic-Search-Kibana를-설치해서-springboot와-연결하자-1
- https://velog.io/@uara67/Spring-ELK-Docker-Elastic-Search-Kibana를-설치해서-springboot와-연결하자-2
- https://velog.io/@uara67/Spring-ELK-es로-구현한-예매검색-api

# 🚀 STACK

Environment

![인텔리제이](   https://img.shields.io/badge/IntelliJ_IDEA-000000.svg?style=for-the-badge&logo=intellij-idea&logoColor=white)
![](https://img.shields.io/badge/Gradle-02303a?style=for-the-badge&logo=gradle&logoColor=white)
![](https://img.shields.io/badge/Postman-ff6c37?style=for-the-badge&logo=postman&logoColor=white)
![깃허브](https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white)
![깃이그노어](https://img.shields.io/badge/gitignore.io-204ECF?style=for-the-badge&logo=gitignore.io&logoColor=white)
![깃](https://img.shields.io/badge/GIT-E44C30?style=for-the-badge&logo=git&logoColor=white)

Development

![스프링부트](https://img.shields.io/badge/SpringBoot-6db33f?style=for-the-badge&logo=springboot&logoColor=white)
![자바](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)

Communication

![슬랙](  https://img.shields.io/badge/Slack-4A154B?style=for-the-badge&logo=slack&logoColor=white)
![노션](https://img.shields.io/badge/Notion-000000?style=for-the-badge&logo=notion&logoColor=white)

# 🏗️프로젝트 설계

### [Wireframe](https://drive.google.com/file/d/1cl51FhT9eB7Fn7WYQunUS5aeoeQceLgx/view?usp=sharing)

![ticket drawio](https://github.com/user-attachments/assets/e3d196e7-c34c-480b-8520-0da74506431c)

### [API Document](https://teamsparta.notion.site/8b2632a9b7ac4fb0a518397e27eb6830)
- 자세한 api명세서는 팀 노션에서 확인할 수 있습니다. 
![image](https://github.com/user-attachments/assets/be815adc-b9d6-4e5e-bb3d-5a39d4494cb1)
![image](https://github.com/user-attachments/assets/d20d10e4-0c4d-4643-9f93-19a47fedea16)

### [ERD diagram](https://www.erdcloud.com/d/5iR9JboxDdHp9rhv4)
![image](https://github.com/user-attachments/assets/c8931c5d-13ba-4120-9876-4e0b613e9af6)

# 🔍 프로젝트 기능 정리

### **회원가입/로그인**

- 회원가입
    - 유저 아이디
    - 비밀번호
    - 권한
        - 회원가입시 일반 유저(USER) 또는 관리자(ADMIN)으로 가입할 수 있습니다.
        - 권한에 따라 사용할 수 있는 기능이 나뉘어집니다.
- 회원탈퇴
    - 조건
        - 탈퇴 처리 시 `비밀번호`를 확인한 후 일치할 때 탈퇴 처리합니다.
        - 탈퇴한 유저의 아이디는 재사용할 수 없고, 복구할 수 없습니다.
- 로그인
    - 가입한 아이디와 비밀번호로 로그인 합니다.

### **메인페이지**

1. 현재 공연기간인 공연에 대한 목록을 볼 수 있습니다.
2. 출연자와 공연명으로 검색이 가능합니다.
3. ADMIN  공연 생성과 공연 삭제
    1. 공연 생성 시 포스터, 공연-출연자 데이터 추가
    2. 공연 삭제 시 해당 공연의 포스터, 공연-출연자 데이터 삭제

### **공연상세페이지**
a. 메인페이지의 공연포스터를 통해 공연상세페이지로 이동이 가능합니다.

b. 공연상세페이지에서 공연관련 정보를 확인할 수 있습니다.
(공연정보, 캐스팅정보, 판매정보, 관람후기, QA게시판)

c. 날짜와 회차를 선택하여 예매할 수 있습니다.

d. 해당 공연의 예매상태를 확인할 수 있습니다.(에매 대기, 예매 가능, 매진)

e. 공연의 출연자(배우)를 확인할 수 있습니다.

### **공연예매페이지**

1. 좌석 조회
2. 공연예매
3. 예매 취소

### **결제페이지**

1. 현재 선택한 공연정보와 사용자 정보가 표시됩니다.
2. 금액과 결제수단(카드)가 표시됩니다.
3. 결제버튼을 누르면 결제창이 뜹니다.

### **마이페이지**

1. 예매한 내역을 취소할 수 있습니다. = 결제취소
2. 이전에 관람한 공연의 정보를 확인할 수 있습니다.
(공연, 날짜, 회차, 좌석정보, 결제정보(카드, 금액))
3. 이전에 관람한 공연의 후기를 작성할 수 있습니다.
4. 개인정보 수정이 가능합니다.
   
### **알림(슬랙)**

1. 공연예매가 완료되면 알림을 전송합니다.
2. 공연취소가 되면 알림을 전송합니다.

### **쿠폰**

1. 회원가입 시 자동으로 기본 쿠폰이 발급됩니다.
2. 쿠폰 발급은 관리자계정만 가능합니다.
3. 쿠폰은 결제 당 한 번 사용할 수 있습니다.
4. 사용한 쿠폰은 재사용이 불가능합니다.
   

# ⚒️트러블슈팅 (추가 및 수정 예정)
- 레디스에서 데이터를 캐싱 시  Java LocalTime을 읽지 못하는 오류
- 레디스에서 객체를 저장 시 객체의 생성자를 읽을 수 없다는 오류
- Jenkins 서버에서 git repository clone 후 application build 과정에서 리소스 부족으로 인한 무한로딩, EC2 접근불가능
- Build Docker 단계에서 Docker 명령어를 찾지 못하는 ‘Docker not found’ 문제
- 쿠폰 락 적용 이슈
- 날짜 데이터 조회 시, 강제정렬되어 표시되는 문제
- 잘못된 캐싱 처리로 인한 데이터 미변경 이슈
- Elastic Search, Dto 통합이슈
- Elastic Search, 의존성 이슈
