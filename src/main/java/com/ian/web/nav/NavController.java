package com.ian.web.nav;

import javax.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import com.ian.web.employee.Employee;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class NavController {	
	
	@GetMapping("/dashboard")
    public String dashboard(HttpSession session, Authentication auth) {
		Employee actor = (Employee) auth.getPrincipal();
        session.setAttribute("actorObj", actor);
                
        return "dashboard";
    }

}
