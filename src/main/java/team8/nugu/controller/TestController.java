package team8.nugu.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import team8.nugu.config.jwt.JWTUtil;
import team8.nugu.dto.TestRequestDto;
import team8.nugu.dto.TestStatusResponseDto;
import team8.nugu.entity.Users;
import team8.nugu.repository.UserRepository;
import team8.nugu.service.TestService;

import java.util.List;

@RestController
@RequestMapping("/tests") // API 명세서에 기반하여 변경할 예정!
@RequiredArgsConstructor
public class TestController {
    private final TestService testService;
    private final JWTUtil jwtUtil;
    private final UserRepository userRepository;

    // 사용자 테스트 생성 여부 확인
    @GetMapping("/status")
    public ResponseEntity<TestStatusResponseDto> checkTestStatus(HttpServletRequest request) {
        // 1. Authorization 헤더에서 토큰 추출 및 검증
        String token = request.getHeader("Authorization");
        if(token == null || token.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String accessToken = token.substring(7);
        String username = jwtUtil.getUsername(accessToken);
        Users user = userRepository.findByUsername(username);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        //2. 사용자의 테스트 생성 여부 확인
        TestStatusResponseDto status = testService.checkTestStatus(user);
        return ResponseEntity.ok(status);
    }

    // 접속자 뷰에서 사용자 테스트 생성 여부 확인
    @GetMapping("/status/{uuid}")
    public ResponseEntity<TestStatusResponseDto> checkTestStatusByUuid(@PathVariable String uuid) {
        try {
            TestStatusResponseDto status = testService.checkTestStatusByUuid(uuid);
            return ResponseEntity.ok(status);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // 퀴즈 생성
    @PostMapping
    public ResponseEntity<Long> createTest(
            @RequestBody TestRequestDto request,
            HttpServletRequest httpRequest
    ) {
        // 1. Authorization 헤더에서 토큰 추출
        String token = httpRequest.getHeader("Authorization");
        if(token == null || token.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 2. Bearer 제거
        String accessToken = token.substring(7);

        // 3. 토큰에서 username 추출
        String username = jwtUtil.getUsername(accessToken);

        // 4. username으로 사용자 조회
        Users user = userRepository.findByUsername(username);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Long testId = testService.createTest(request, user);
        return ResponseEntity.ok(testId);
    }

    // UUID로 테스트 결과 조회
    @GetMapping("/{uuid}/answers")
    public ResponseEntity<List<String>> getTestAnswers (@PathVariable String uuid) {
        try {
            // UUID로 테스트 결과 조회
            List<String> answers = testService.getTestAnswers(uuid);

            if (answers == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            return ResponseEntity.ok(answers);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}