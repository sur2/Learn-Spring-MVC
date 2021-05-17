# Learn-Spring-MVC



## 웹 애플리케이션

| 웹 서버(Web Server)                           | 웹 애플리케이션(Web Application) |
| --------------------------------------------- | -------------------------------- |
| 정적 리소스(파일: html, css, javascript) 실행 | 애플리케이션 로직(Java) 실행 |



### 웹 시스템 구성

Web Server - Web Application Server(WAS)- DB

- Web Server : 정적 리소스 담당
- WAS : 애플리케이션 로직 담당
- DB :  데이터베이스 CRUD



### 서블릿

웹 브라우저 - 웹 애플리케이션 서버(+ 서블릿 컨테이너: HTTP request와 response 객체 처리)

#### 서블릿 컨테이너

- **싱글톤으로 관리됨**
  - 최초 로딩 시점에 서블릿 객체를 미리 만들어두고 재활용하는 것이 효율적임
  - **공유 변수 사용 주의**
- 서블릿 객체를 생성, 초기화, 호출, 종료하는 생명주기  관리
- 동시 요청을 위한 멀티 쓰레드 처리 지원



### 동시 요청 - 멀티 쓰레드

#### 쓰레드

- 애플리케이션 코드를 하나씩 순차적으로 실행하는 것

#### 멀티 쓰레드

**요청 마다 쓰레드가 생성된다면?**

- 장점
  - 동시 요청 처리 가능
  - 독립적으로 사용 할 수 있음(하나의 쓰레드가 지연되어도 다른 쓰레드는 정상 작동)
- 단점
  - 생성 비용이 비쌈(쓰레드가 많을수록 응답속도가 느려짐)
  - 생성에 제한이 없음

#### 쓰레드풀

- 미리 쓰레드를 생성하고 보관하기 때문에 멀티 쓰레드의 단점을 보완 

예)

1. 200개의 쓰레드를 쓰레드풀에 보관

2. 필요할 때 마다 쓰레드를 대여

3. 다 쓴 쓰레드는 다시 쓰레드 풀에 반환

- 실무 Tip!
  - WAS의 주요 튜닝 포인트는 최대 쓰레드(max thread) 수이다.
  - 최대 쓰레드가 낮다면, 서버의 리소스는 여유롭지만, 클라이언트에 대한 응답 지연
  - 최대 쓰레드가 높다면, 동시 요청이 많을 때 CPU, 메모리 리소스가 임계점을 초과하여 서버가 다운
  - 클라우드 서버를 사용한다면 서버를 증설하고 튜닝, 그렇지 않다면 바로 튜닝
  - 애플리케이션 로직, CPU, 메모리, IO 리소스 상황을 모두 고려하여 쓰레드 개수를 정한다.(성능 테스트를 권장)

**멀티 쓰레드 환경에서 싱글톤 객체(서블릿, 스프링 빈)는 주의해서 사용**



### HTML, HTTP API, CSR, SSR

#### HTTP API

- HTML이 아닌 데이터를 전달

#### CSR 클라이언트 사이드 렌더링

- HTML 결과를 javascript를 사용해서 동적으로 생성

#### SSR 서버 사이드 렌더링

- 서버에서 최종 HTML을 생성하여  클라이언트에 전달



## 서블릿

``@ServletComponentScan``: 하위 폴더 전체에서 Servlet을 찾아서 등록(서블릿 자동 등록)

``@WebServlet``: url패턴에 대한 service 메서드 실행



### 1. HttpServletRequest

HTTP 요청 메시지를 파싱

- START LINE
- header
- body

부가기능

- 임시 저장소 기능(해당 HTTP 요청의 시작부터 끝날 때 까지 유지)
  - 저장 ``request.setAttribute(name, value)``
  - 조회 ``request.getAttribute(name, vlaue)``
- 세션 관리 기능
  - ``request.getSession(crate: true)``

 

### 2. HTTP 요청 데이터

#### 1. GET 쿼리 파라미터

- 조회: ``request.getParameter(name)``

- 메시지 바디가 없음
- URL에 ``?``를 시작으로 ``Key = value``를 사용, 추가 파라미터는 ``&``로 구분
- 같은 키의 복수 파라미터 조회 가능 ``request.getParameterValues(name)``

#### 2. POST HTML Form

- 메시지 바디에 쿼리 파라미터 형식으로 전달. ``username=hello&password=123!@#``  
- context-type: ``application/x-www-form-urlencoded``
  context-type - 바디에 포함된 데이터가 어떤 형식인지 지정하는 역할
- 조회는 GET과 동일. ``request.getParameter(name)``

#### 3. API 메시지 바디

- 단순 텍스트
  - **HTTP message body**에 데이터를 직접 담아서 요청
  - POST, PUT, PATCH
  
- JSON
  
  - JSON 형식에 맞춰서 요청하면 됨.
  
  - Jackson 라이브러리를 사용하여 객체로 변환가능
  
    ```java
    private ObjectMapper objectMapper = new ObjectMapper();
    
        @Override
        protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            ServletInputStream inputStream = request.getInputStream();
            String messageBody = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
    
            System.out.println("messageBody" + messageBody);
    
            // Jackson Library - JSON Parser
            HelloData helloData = objectMapper.readValue(messageBody, HelloData.class);
            System.out.println("helloData = " + helloData);
        }
    ```
  
- 조회: ``request.getInputStream()``



### 3. HTTP 응답 데이터

#### 1. 기본 사용법

- response headers를 구성할 수 있는 메서드: ``response.setHeader(name, value)``
- 편의 메서드
  - content ``response.setContentType(type);``, ``response.setCharaterEncoding(encode);``
  - cookie ``Cookie cookie = new Cookie(name, value);``
  - redirect ``response.sendRedirect(url);``

#### 2. 응답 데이터

- 단순 텍스트

  - **Content-Type: text/plain;**charset=utf-8
  -  ``PrintWriter writer = response.getWriter(); writer.println(text);``

- HTML

  - **Content-Type: text/html;**charset=utf-8
  -  HTML 형식의 문자열 작성, 텍스트와 동일한 ``response.getWriter();`` 메서드를 사용하여 작성 

- API JSON

  - **Content-Type: application/json**
  - 반환할 객체는 ObjectMapper로 json형식으로 변환 후 반환, ``response.getWriter().write(objectToString);``
  -  ``application/json``은 스펙상 utf-8 형식을 사용하도록 정의. ``charset=utf-8`` 과 같은 추가 파라미터를 지원하지 않음.



### 4. JSP

#### 1. 사용법

- HTML 형식 + Java 코드 사용 가능
- 확장자 ``.jsp``
- JSP 문서를 알리며 첫줄에 삽입 ``<%@ page contentType="text/html;charset=UTF-8" language="java" %>``
- Java의 import 시 ``<%@ page import="hello.servlet.domain.member.MemberRepository" %>``
- Java 코드 입력 ``<% ~~ %>`` 
- Java 코드 출력 ``<%= ~~ %>``



### 5. MVC 패턴

#### 1. 탄생 이유

- 하나의 서블릿, JSP에서 비즈니스 로직과 뷰 렌더링까지 모두 처리하게 되면, 너무 많은 역할을 하게 되어 유지보수가 어려워진다.
- **변경의 라이프 사이클**: UI와 비즈니스 로직은 변경의 라이프 사이클이 다르다. 유지보수하기 좋지 않다.
- JSP와 같은 뷰 템플릿은 화면 렌더링에 최적화 되어있다. 다른 일을 하기에는 부적절하다.

#### 2. Model View Contoller

- **컨트롤러**: 비즈니스 로직 호출(서비스, 리포지토리에서 비즈니스 로직 수행), 모델에게 데이터 전달, 뷰에게 제어권 전달
- **모델**: 컨트롤러로 부터 데이터를 전달 받음, 뷰에게 데이터를 전달하기 위함
- **뷰**: 모델의 데이터를 참조하여 출력

#### 3. MVC 패턴 적용

- 컨트롤러 - 서블릿
  - ``dispatcher.forward()``: 다른 서블릿이나 JSP로 이동할 수 있는 기능, 서버 내부에서 다시 호출이 발생
  - ``WEB-INF``: 이 경로안에 JSP가 있으면 외부에서 직접 호출할 수 없음, 반드시 컨트롤러를 통해서 JSP를 호출
  - **redirect vs forward**
    - 리다이렉트: 웹브라우저에 응답이  나갔다가, 클라이언트가 리다이렉트 경로로 다시 요청한다. 
      화면이 깜빡거림을 클라이언트가 인지할 수 있고, URL 경로도 변경된다. (객체 재사용 불가능)
    - 포워드: 서버 내부에서 일어나는 호출, 클라이언트가 인지할 수 없음. (객체 재사용 가능)
- 모델 - ``HttpServletRequest``객체, request의 내부 저장소를 가지고 있음.
  보관: ``request.setAttribute()``, 조회: ``request.getAttribute()``
  - ``${name}``: ``request.getAttribute(name)``를 JSP에서 간단하게 호출하는 표현식 .
  - JSP는 JSTL이라는 자바 코드를 직접 삽입하지 않고 로직을 내장하는 표현식이 있다.
    JSP 뿐만 아니라 다른 뷰 템플릿들도 이러한 표현식을 가지고 있다.
- 뷰 - HTML 등...

#### 4. MVC 패턴의 한계

- 포워드의 중복
- ViewPath의 중복
  - prefix: ``/WEB-INF/directory...``
  - suffix: ``.jsp``, ``.html``
- 사용하지 않는 코드
- 공통 처리가 어려움(**프론트 컨트롤러**가 요구됨)



## MVC 프레임워크 만들기

### 1. 프론트 컨트롤러 패턴

- 프론트 컨트롤러 서블릿 하나가 공통으로 클라이언트의 요청을 받음.
- 프론트 컨트롤러가 요청에 맞는 컨트롤러를 찾아서 호출.
- 프론트 컨트롤러를 제외한 나머지 컨트롤러는 서블릿을 사용하지 않아도 됨.
- **스프링 웹 MVC의 핵심은 프론트 컨트롤러(DispatcherServlet)**

#### 1. 프론트 컨트롤러 도입 - v1

- 서블릿과 비슷한 모양의 컨트롤러  인터페이스 도입.

- 각 컨트롤러는 인터페이스를 구현하면 됨.

- 공통으로 HTTP 요청을 받는 서블릿이 인터페이스를 구현한 각 컨트롤러를 호출.

- 각 컨트롤러에서 View(웹 페이지) 호출

  ```java
  String viewPath = "/WEB-INF/views/new-form.jsp";
          RequestDispatcher dispatcher = request.getRequestDispatcher(viewPath);
          dispatcher.forward(request, response);
  ```

#### 2. View 분리 - v2

- 각 컨트롤러에서 View(웹 페이지)를 호출하던 부분을 프론트 컨트롤러로 옮김
  - View(렌더링)를 담당하는 클래스 작성(``MyView``), 각 컨트롤러는 View 객체를 반환
  - 프론트 컨트롤러에서 View의 렌더링 기능 실행(메서드 호출)

#### 3. Model 추가 - v3

- 서블릿 종속성 제거
  - 불필요한 ``HttpServletRequest, HttpServletResponse``  코드 제거
- 뷰 이름 중복 제거
  - 뷰 리졸버가 추가됨, 컨트롤러가 반환하는 논리 뷰 이름을 실제 물리 뷰 경로로 변경
- 프론트 컨트롤러의 역할이 증가

#### 4. 단순하고 실용적인 컨트롤러 - v4

- ModelView 객체 생성 및 반환 개선
  - 컨트롤러가 ``ModelView``를 반환하지 않고, ``ViewName``만 반환한다.









