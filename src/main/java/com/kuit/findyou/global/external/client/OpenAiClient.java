package com.kuit.findyou.global.external.client;

import com.kuit.findyou.domain.breed.dto.response.BreedAiDetectionResponseDTO;
import com.kuit.findyou.global.common.exception.CustomException;
import com.kuit.findyou.global.external.dto.OpenAiResponse;
import com.kuit.findyou.global.external.util.OpenAiParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

import static com.kuit.findyou.global.common.response.status.BaseExceptionResponseStatus.BREED_ANALYSIS_FAILED;

@Component
@Slf4j
public class OpenAiClient {

    private final RestClient openAiRestClient;

    private static final String PROMPT = """
            Generate a response in the following format:
             \
            Species,Breed,Color1,Color2,Color3,...

             \
            - The species must be one of the following: "개", "고양이", "기타".
             \
            - The breed must be exactly one, and it must match the species category:

             \
            If the species is "개":
             고든 세터, 골든 리트리버, 그레이 하운드, 그레이트 덴, 그레이트 피레니즈, 그리펀 벨지언, 꼬똥 드 뚤레아, 네오폴리탄 마스티프, 노르포크 테리어, 노리치 테리어, 노퍽 테리어, 뉴펀들랜드, 달마시안, 댄디 딘몬트 테리어, 도고 까니리오, 도고 아르젠티노, 도베르만, 도사, 도사 믹스견, 동경견, \
            라고토 로마그놀로, 라브라도 리트리버, 라사 압소, 래빗 닥스훈트, 랫 테리어, 러시안 토이, 러프콜리, 레이크랜드 테리어, 로디지안 리즈백, 로트와일러, 로트와일러 믹스견, 마리노이즈, 마스티프, 말라뮤트, 말티즈, 맨체스터테리어, 미니어쳐 닥스훈트, 미니어쳐 불 테리어, \
            미니어쳐 슈나우저, 미니어쳐 푸들, 미니어쳐 핀셔, 미디엄 푸들, 미텔 스피츠, 믹스견, 바센지, 바셋 하운드, 버니즈 마운틴 독, 베들링턴 테리어, 벨기에 쉽독, 벨기에 테뷰런, 벨지안 셰퍼드 독(그로넨달), 벨지안 셰퍼드 독(라케노이즈), 벨지안 셰퍼드 독(마리노이즈), \
            벨지안 셰퍼드 독(테르뷰렌), 보더 콜리, 보르조이, 보스턴 테리어, 복서, 볼로네즈, 부비에 데 플랑드르, 브뤼셀그리펀, 브리타니 스파니엘, 블랙 테리어, 비글, 비숑 프리제, 비어디드 콜리, 비즐라, 빠삐용(콘티넨탈 토이 스파니엘), 사모예드, 살루키, 삽살개, 샤페이, 세인트 버나드, \
            센트럴 아시안 오브차카, 셔틀랜드 쉽독, 셰퍼드, 슈나우져, 스무스콜리, 스코티쉬 테리어, 스코티시 디어하운드, 스키퍼키, 스태퍼드셔 불 테리어, 스태퍼드셔 불 테리어 믹스견, 스탠다드 닥스훈트, 스탠다드 푸들, 스피츠, 슬루기, 시바, 시베리안 허스키, 시잉프랑세즈, \
            시츄, 시코쿠, 실리햄 테리어, 실키테리어, 아나톨리안 셰퍼드, 아메리칸 불독, 아메리칸 스태퍼드셔 테리어, 아메리칸 스태퍼드셔 테리어 믹스견, 아메리칸 아키다, 아메리칸 에스키모, 아메리칸 코카 스파니엘, 아메리칸 핏불 테리어, 아메리칸 핏불 테리어 믹스견, 아메리칸불리, \
            아이리쉬 레드 앤 화이트 세터, 아이리쉬 세터, 아이리쉬 소프트 코티드 휘튼 테리어, 아이리쉬 울프 하운드, 아자와크, 아키다, 아펜핀셔, 아프간 하운드, 알라스칸 말라뮤트, 에어델 테리어, 오브차카, 오스트랄리안 셰퍼드 독, 오스트랄리안 캐틀 독, 오스트레일리안 케틀독, 오스트레일리안 켈피, \
            올드 잉글리쉬 불독, 올드 잉글리쉬 쉽독, 와이마라너, 요크셔 테리어, 울프독, 웨스트 시베리언 라이카, 웨스트하이랜드화이트테리어, 웰시 스프링어 스패니얼, 웰시 코기 카디건, 웰시 코기 펨브로크, 웰시 테리어, 유쿠시안 라이카, 이스트 시베리언 라이카, 이탈리안 그레이 하운드, 잉글리쉬 세터, 잉글리쉬 스프링거 스파니엘, \
            잉글리쉬 코카 스파니엘, 잉글리쉬 포인터, 자이언트 슈나우져, 재패니즈 스피츠, 잭 러셀 테리어, 저먼 셰퍼드 독, 저먼 쇼트헤어드 포인터, 저먼 와이어헤어드 포인터, 저먼 헌팅 테리어, 제주개, 제페니스 친, 제페니즈칭, 중앙아시안 셰퍼드, 진도견, 차우차우, 차이니즈 크레스티드 독, 체코슬로바이칸 울프독, \
            치와와, 카네 코르소, 카레리안 베어독, 카이훗, 캉갈 셰퍼드 독, 캐벌리어 킹 찰스 스파니엘, 케나디언 에스키모 독, 케니스펜더, 케리 블루 테리어, 케언 테리어, 코리아 트라이 하운드, 코리안 마스티프, 코카 스파니엘, 코카 푸, 코카시안 오브차카(코카시안 셰퍼드 독), 클라인스피츠, 키슈, 키스 훈드, 타이 리지벡 독, \
            토이 맨체스터 테리어, 토이 푸들, 티베탄 마스티프, 파라오 하운드, 파슨 러셀 테리어, 팔렌(콘티넨탈 토이 스파니엘), 퍼그, 페키니즈, 페터데일테리어, 포르투갈 워터 도그, 포메라니안, 폭스 테리어(스무스), 폭스 테리어(와이어), 풀리, 풍산견, 프레사까나리오, 프렌치 불독, 프렌치 브리타니, 프티 바세 그리퐁 방댕, \
            플랫 코티드 리트리버, 플롯하운드, 피레니안 마운틴 독, 필라 브라질레이로, 핏불테리어, 핏불테리어 믹스견, 허배너스, 화이트 스위스 세퍼드, 화이트리트리버, 화이트테리어, 휘펫

             \
            If the species is "고양이":
             러시안 블루, 페르시안, 메인쿤, 브리티시 쇼트헤어, 샴, 벵갈, 아메리칸 쇼트헤어, 스코티시폴드, 노르웨이 숲, 터키시 앙고라, 기타, 니벨룽, 데본 렉스, 레그돌, 레그돌-라가머핀, 맹크스, 먼치킨, 믹스묘, 발리네즈, \
            버만, 봄베이, 브리티쉬롱헤어, 사바나캣, 샤트룩스, 셀커크 렉스, 소말리, 스노우 슈, 스핑크스, 시베리안 포레스트, 싱가퓨라, 아비시니안, 재패니즈밥테일, 통키니즈, 페르시안-페르시안 친칠라, 하바나 브라운, 하일랜드 폴드, 한국 고양이

             \
            If the species is "기타":
             기타축종

             \
            - Colors must be one or more, separated by commas (",").
             \
            The color must be chosen from the following fixed list:
             검은색, 노란색, 점박이, 하얀색, 갈색, 회색, 적색, 기타

             \
            - There should be no spaces between commas in the color list.

             \
            **Example input & expected response:**
             개,골든 리트리버,노란색
             고양이,러시안 블루,회색,검은색
             기타,기타축종,하얀색""";

    public OpenAiClient(@Qualifier("openAiRestClient") RestClient openAiRestClient) {
        this.openAiRestClient = openAiRestClient;
    }

    public BreedAiDetectionResponseDTO analyzeImage(String imageUrl) {
        try {
            Map<String, Object> body = Map.of(
                    "model", "gpt-4o",
                    "max_tokens", 300,
                    "messages", List.of(
                            Map.of(
                                    "role", "user",
                                    "content", List.of(
                                            Map.of(
                                                    "type", "image_url",
                                                    "image_url", Map.of("url", imageUrl)
                                            ),
                                            Map.of(
                                                    "type", "text",
                                                    "text", PROMPT
                                            )
                                    )
                            )
                    )
            );


            OpenAiResponse response = openAiRestClient.post()
                    .uri("")
                    .body(body)
                    .retrieve()
                    .body(OpenAiResponse.class);

            if (response == null || response.choices().isEmpty()) {
                log.warn("[OpenAI 응답 없음] imageUrl={}", imageUrl);
                throw new CustomException(BREED_ANALYSIS_FAILED);
            }

            String content = response.choices().get(0).message().content();

            return new BreedAiDetectionResponseDTO(
                    OpenAiParser.parseSpecies(content),
                    OpenAiParser.parseBreed(content),
                    OpenAiParser.parseColors(content)
            );
        } catch (Exception e) {
            log.error("[OpenAI Vision API 호출 실패] imageUrl={}", imageUrl, e);
            throw new CustomException(BREED_ANALYSIS_FAILED);
        }
    }
}
