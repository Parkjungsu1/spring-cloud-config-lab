# 🚀 Spring Cloud Config Lab
이 프로젝트는 분산 시스템에서 설정 정보를 중앙 집중식으로 관리하고, 설정 변경 시 재배포 없이 모든 클라이언트에 실시간 전파(Refresh)하는 구조를 실습한다

## 📌 개요
- 중앙 관리: 서비스가 늘어남에 따라 각 서비스의 설정을 개별 관리하는 한계를 극복.

- 실시간 갱신: Spring Cloud Bus와 RabbitMQ를 이용해 설정 변경 시 서버 재시작 없이 반영.

- 보안 강화: 설정 정보(DB 비밀번호 등)를 코드와 분리하여 별도 저장소(Git, Vault 등)에 보관.

🏗 전체 아키텍처 및 흐름
1. Git Push: 설정 저장소(Git Repo)의 데이터 변경.

2. GitHub Webhook: Config Server의 /webhook 엔드포인트로 알림 송신 (ngrok 활용).

3. Internal Trigger: WebhookController가 내부적으로 /actuator/busrefresh 호출.

4. Event Publish: Config Server가 RabbitMQ의 springCloudBus Exchange로 이벤트 발행.

5. Broadcast: 연결된 모든 Config Client가 이벤트를 수신.

6. Refresh: 각 클라이언트가 Config Server에서 최신 설정을 받아오고 @RefreshScope 빈을 갱신.

## 🛠 Prerequisites
1. 설정 저장소 (Git Repo)

- 예: application.yml, config-dev.yml 등 서비스명 기반 파일.

2. Message Broker
- RabbitMQ 실행 중 

3. 외부 노출 도구 (Local Test용)
- ngrok http 8888 (GitHub Webhook이 로컬 서버에 접근하기 위함)

## 💻 Config Server 설정
### Dependencies (build.gradle)
Gradle
```
implementation 'org.springframework.cloud:spring-cloud-config-server'
implementation 'org.springframework.cloud:spring-cloud-starter-bus-amqp'
implementation 'org.springframework.boot:spring-boot-starter-actuator'
```

Main Class
```
@EnableConfigServer
@SpringBootApplication
public class ConfigServerApplication { ... }
```

Webhook Controller 

/actuator/busrefresh는 application/json 타입을 요구하므로 헤더 설정을 반드시 포함합니다.

```
@PostMapping("/webhook")
public void handleWebhook() {
    RestTemplate restTemplate = new RestTemplate();
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON); // 415 에러 방지용 비자(Visa)
    
    HttpEntity<String> entity = new HttpEntity<>("{}", headers);
    restTemplate.postForObject("http://localhost:8888/actuator/busrefresh", entity, String.class);
}
```

## 📱 Config Client 설정

Gradle
```
implementation 'org.springframework.cloud:spring-cloud-starter-config'
implementation 'org.springframework.cloud:spring-cloud-starter-bus-amqp'
```
application.yml
```
YAML
spring:
  config:
    import: "configserver:http://localhost:8888"
  cloud:
    bus:
      enabled: true # RabbitMQ를 통한 이벤트 수신 활성화
```

test controller (예시)
```
@RestController
@RefreshScope // 설정 변경 시 이 Bean을 다시 생성하여 최신 값을 주입받음
public class ConfigController {

    @Value("${custom.message}")
    private String message;

    @GetMapping("/api")
    public String check() {
        return message;
    }
}
```
## ⚠️ Troubleshooting & Lessons Learned
1. 415 Unsupported Media Type
- 문제: RestTemplate 호출 시 Content-Type 미지정으로 에러 발생.

- 해결: MediaType.APPLICATION_JSON 헤더를 명시적으로 추가하여 Actuator 엔드포인트 규격을 맞춤.
