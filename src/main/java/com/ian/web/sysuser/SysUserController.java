package com.ian.web.sysuser;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class SysUserController {
	
	private SysUserRepository sysUserRepository;
	
	public SysUserController (SysUserRepository sysUserRepository) {
		this.sysUserRepository = sysUserRepository;
	}
	
	@GetMapping("/users")
	public String listAll(Model model) {
		Iterable<SysUser> users = sysUserRepository.findAll();
		model.addAttribute("users", users);
		return "users/users_list";
	}
	
}
