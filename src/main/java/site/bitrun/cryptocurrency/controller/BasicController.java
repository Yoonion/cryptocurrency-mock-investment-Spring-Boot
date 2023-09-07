package site.bitrun.cryptocurrency.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import site.bitrun.cryptocurrency.global.api.coinmarketcap.domain.CryptoRank;
import site.bitrun.cryptocurrency.domain.Member;
import site.bitrun.cryptocurrency.dto.MemberLoginForm;
import site.bitrun.cryptocurrency.dto.MemberRegisterForm;
import site.bitrun.cryptocurrency.global.api.coinmarketcap.service.CryptoService;
import site.bitrun.cryptocurrency.global.api.upbit.domain.UpbitMarket;
import site.bitrun.cryptocurrency.global.api.upbit.dto.UpbitMarketDto;
import site.bitrun.cryptocurrency.global.api.upbit.service.UpbitService;
import site.bitrun.cryptocurrency.service.MemberService;

import java.util.ArrayList;
import java.util.List;


@Controller
public class BasicController {

    private final MemberService memberService;
    private final CryptoService cryptoService;
    private final UpbitService upbitService;

    @Autowired
    public BasicController(MemberService memberService, CryptoService cryptoService, UpbitService upbitService) {
        this.memberService = memberService;
        this.cryptoService = cryptoService;
        this.upbitService = upbitService;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////// API DB SAVE /////////////////////////////////
    /////////////// 코인마켓캡 api 호출해 DB 저장 - 임시 .. //////////////////
    @GetMapping("/crypto/rank/api/save")
    public String cryptoRankApi() {
        cryptoService.saveCryptoRankList();
        return "redirect:/";
    }

    ///////////////// 업비트 api 호출 - 거래가능 마켓 목록 DB 저장 - 임시 .. ///////////////
    @GetMapping("/upbit/api/save")
    public String upbitMarketApi() {
        upbitService.saveUpbitMarket();
        return "redirect:/";
    }
    ////////////////////////// API DB SAVE //////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////

    // 메인 페이지
    @GetMapping("/")
    public String main(Model model) {
        List<CryptoRank> cryptoRankList = cryptoService.getCryptoRankList();
        model.addAttribute("cryptoRankList", cryptoRankList);

        return "index";
    }

    // 거래소
    @GetMapping("/trade/order")
    public String viewChart(@RequestParam(name="code", required = false) String code, Model model) {

        // 파라미터 없으면 기본 비트코인으로 redirect 시킴
        if (StringUtils.isEmpty(code)) {
            return "redirect:/trade/order?code=KRW-BTC";
        }

        // 쿼리 파라미터로 해당하는 암호화폐의 정보를 1개 가져온다.
        UpbitMarket findUpbitCryptoOne = upbitService.getUpbitMarketOne(code);
        model.addAttribute("upbitCryptoInfo", findUpbitCryptoOne);

        // 오른쪽 side nav 를 위한 전체 리스트
        List<UpbitMarket> upbitMarketList = upbitService.getUpbitMarketList();
        model.addAttribute(upbitMarketList);

        // upbit websocket 요청 json 부분 - 암호화폐 list json 요청에 넣어줄 것임
        List<String> marketListString = new ArrayList<>();
        for (UpbitMarket upbitMarket : upbitMarketList) {
            marketListString.add(upbitMarket.getMarket());
        }
        // System.out.println(marketListString);
        model.addAttribute("marketListString", marketListString);

        return "trade/order";
    }

    // 회원가입 view
    @GetMapping("/member/register")
    public String memberRegisterForm(@ModelAttribute MemberRegisterForm memberRegisterForm) {
        return "memberRegisterForm";
    }

    // 회원가입
    @PostMapping("/member/register")
    public String memberRegister(@Validated @ModelAttribute MemberRegisterForm memberRegisterForm, BindingResult bindingResult, HttpServletRequest request) {

        if (bindingResult.hasErrors()) {
            return "memberRegisterForm";
        }

        // 비밀번호 확인
        if (!memberRegisterForm.getPassword().equals(memberRegisterForm.getPassword2())) {
            bindingResult.rejectValue("password2", "differentPassword", "패스워드가 일치하지 않습니다.");
            return "memberRegisterForm";
        }

        // 중복 체크
        // true 일 경우 중복
        if (memberService.memberCheckDuplicate(memberRegisterForm.getEmail())) {
            bindingResult.reject("emailDuplicate", "이미 존재하는 회원입니다."); // global error
            return "memberRegisterForm";
        }

        Member newMember = new Member(memberRegisterForm.getUsername(), memberRegisterForm.getEmail(), memberRegisterForm.getPassword());
        memberService.memberRegister(newMember);

        // 성공했다면 로그인까지 처리한다.
         memberService.memberLogin(memberRegisterForm.getEmail(), memberRegisterForm.getPassword(), request);

        return "redirect:/";
    }

    // 로그인 view
    @GetMapping("/member/login")
    public String memberLoginForm(@ModelAttribute MemberLoginForm memberLoginForm) {
        return "memberLoginForm";
    }

    // 로그인 처리
    @PostMapping("/member/login")
    public String memberLogin(@Validated @ModelAttribute MemberLoginForm memberLoginForm, BindingResult bindingResult, HttpServletRequest request) {

        if (bindingResult.hasErrors()) {
            return "memberLoginForm";
        }

        Member loginMember = memberService.memberLogin(memberLoginForm.getEmail(), memberLoginForm.getPassword(), request);

        // 로그인 실패시
        if (loginMember == null) {
            bindingResult.reject("loginFail", "아이디 또는 비밀번호가 맞지 않습니다."); // global error
            return "memberLoginForm";
        }

        return "redirect:/";
    }

    // 로그아웃
    @GetMapping("/member/logout")
    public String memberLogout(HttpServletRequest request) {
        memberService.memberLogout(request);

        return "redirect:/";
    }

}
