package com.djamware.mynotes.controllers;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import com.djamware.mynotes.models.Notes;
import com.djamware.mynotes.models.User;
import com.djamware.mynotes.repositories.NotesRepository;
import com.djamware.mynotes.services.CustomUserDetailsService;

@Controller
public class NotesController {


	private final CustomUserDetailsService userService;
	private final NotesRepository noteRepository;

	public static final String ATTR_CURRENT_USER = "currentUser";
	public static final String ATTR_FULL_NAME = "fullName";
	public static final String ATTR_ADMIN_MESSAGE = "adminMessage";
	public static final String ATTR_VALUE_FULL_NAME = "Welcome ";
	public static final String ATTR_VALUE_ADMIN_MESSAGE = "Content Available Only for Users with Admin Role";


	@Autowired
	NotesController(CustomUserDetailsService userService, NotesRepository noteRepository){
		this.userService = userService;
		this.noteRepository = noteRepository;
	}

	@GetMapping(value = "/notes")
	public ModelAndView notes() {
		ModelAndView modelAndView = new ModelAndView();
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = userService.findUserByEmail(auth.getName());
		modelAndView.addObject("notes", noteRepository.findAll());
		modelAndView.addObject(ATTR_CURRENT_USER, user);
		modelAndView.addObject(ATTR_FULL_NAME, ATTR_VALUE_FULL_NAME + user.getFullname());
		modelAndView.addObject(ATTR_ADMIN_MESSAGE, ATTR_VALUE_ADMIN_MESSAGE);
		modelAndView.setViewName("notes");
		return modelAndView;
	}

	@RequestMapping("/notes/create")
	public ModelAndView create() {
		ModelAndView modelAndView = new ModelAndView();
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = userService.findUserByEmail(auth.getName());
		modelAndView.addObject(ATTR_CURRENT_USER, user);
		modelAndView.addObject(ATTR_FULL_NAME, ATTR_VALUE_FULL_NAME + user.getFullname());
		modelAndView.addObject(ATTR_ADMIN_MESSAGE, ATTR_VALUE_ADMIN_MESSAGE);
		modelAndView.setViewName("create");
		return modelAndView;
	}

	@RequestMapping("/notes/save")
	public String save(@RequestParam String title, @RequestParam String content) {
		Notes note = new Notes();
		note.setTitle(title);
		note.setContent(content);
		note.setUpdated(new Date());
		noteRepository.save(note);

		return "redirect:/notes/show/" + note.getId();
	}

	private ModelAndView setModelAndViewCommonData (String text, Long id) {
		ModelAndView modelAndView = new ModelAndView();
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = userService.findUserByEmail(auth.getName());
		modelAndView.addObject(ATTR_CURRENT_USER, user);
		modelAndView.addObject(ATTR_FULL_NAME, ATTR_VALUE_FULL_NAME + user.getFullname());
		modelAndView.addObject(ATTR_ADMIN_MESSAGE, ATTR_VALUE_ADMIN_MESSAGE);
		modelAndView.addObject("note", noteRepository.findById(id).orElse(null));
		modelAndView.setViewName(text);
		return modelAndView;
	}

	@RequestMapping("/notes/show/{id}")
	public ModelAndView show(@PathVariable Long id) {
		return setModelAndViewCommonData("show", id);
	}

	@RequestMapping("/notes/delete")
	public String delete(@RequestParam Long id) {
		Notes note = noteRepository.findById(id).orElse(null);
		if(note != null){
		noteRepository.delete(note);
		}

		return "redirect:/notes";
	}

	@RequestMapping("/notes/edit/{id}")
	public ModelAndView edit(@PathVariable Long id) {
		return setModelAndViewCommonData("edit", id);
	}

	@RequestMapping("/notes/update")
	public String update(@RequestParam Long id, @RequestParam String title, @RequestParam String content) {
		Notes note = noteRepository.findById(id).orElse(null);
		if (note == null){
			return "redirct:/notes";
		}
		note.setTitle(title);
		note.setContent(content);
		note.setUpdated(new Date());
		noteRepository.save(note);

		return "redirect:/notes/show/" + note.getId();
	}

}
