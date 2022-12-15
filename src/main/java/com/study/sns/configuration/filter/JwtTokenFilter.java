package com.study.sns.configuration.filter;





import com.study.sns.model.User;
import com.study.sns.service.UserService;
import com.study.sns.util.JwtTokenUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {

    private  final UserService userService;
    private final String key;

    private final static List<String>TOKEN_IN_PARAM_URLS = List.of("/api/v1/users/alarm/subscribe");
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        //get header
        final String token;





        try {
            if(TOKEN_IN_PARAM_URLS.contains(request.getRequestURL())){
                log.info("Request with {} check the query param",request.getRequestURL());
                token=request.getQueryString().split("=")[1].trim();
            }else {
                final String header = request.getHeader(HttpHeaders.AUTHORIZATION);
                if (header == null || !header.startsWith("Bearer ")) {
                    log.error("Error occurs while getting header.header is null or invaild {}",request.getRequestURL());
                    filterChain.doFilter(request, response);
                    return;
                }
                token = header.split(" ")[1].trim();
            }

           //TODO: check token is valid
           if(JwtTokenUtils.isExpired(token,key)){
               log.error("key is expired");
               filterChain.doFilter(request,response);
               return;

            }
            //get userName from token
            String userName = JwtTokenUtils.getUserName(token,key);
            //check the user is valid
            User user =  userService.loadUserByUserName(userName);
            //TODO: get username from token
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    user, null,user.getAuthorities());

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

         }catch(RuntimeException e){
            log.error("Error occur s while validating. {}", e.toString());
            filterChain.doFilter(request,response);
            return;
        }
        filterChain.doFilter(request,response);
    }
}
