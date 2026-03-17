package com.djamware.mynotes.controllers;

import com.djamware.mynotes.models.User;
import com.djamware.mynotes.models.UserDTO;
import com.djamware.mynotes.services.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;

@Controller
public class AuthController {

	private final CustomUserDetailsService userService;

	@Autowired
	public AuthController(CustomUserDetailsService userService){
		this.userService = userService;
	}


	@GetMapping(value = "/login")
	public ModelAndView login() {
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("login");
		return modelAndView;
	}

	@GetMapping(value = "/signup")
	public ModelAndView signup() {
		ModelAndView modelAndView = new ModelAndView();
		User user = new User();
		modelAndView.addObject("user", user);
		modelAndView.setViewName("signup");
		return modelAndView;
	}

	@PostMapping(value = "/signup")
	public ModelAndView createNewUser(@Valid UserDTO userDTO, BindingResult bindingResult) {
		ModelAndView modelAndView = new ModelAndView();
		User userExists = userService.findUserByEmail(userDTO.getEmail());
		if (userExists != null) {
			bindingResult.rejectValue("email", "error.user",
					"There is already a user registered with the username provided");
		}
		if (bindingResult.hasErrors()) {
			modelAndView.setViewName("signup");
		} else {

			User newUser = new User();
			newUser.setEmail(userDTO.getEmail());

			userService.saveUser(newUser);
			modelAndView.addObject("successMessage", "User has been registered successfully");
			modelAndView.addObject("user", new User());
			modelAndView.setViewName("login");

		}
		return modelAndView;
	}

}
