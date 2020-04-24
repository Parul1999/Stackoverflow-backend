package com.upgrad.stackoverflow.api.controller;

import com.upgrad.stackoverflow.api.model.*;
import com.upgrad.stackoverflow.service.business.QuestionBusinessService;
import com.upgrad.stackoverflow.service.entity.QuestionEntity;
import com.upgrad.stackoverflow.service.exception.AuthorizationFailedException;
import com.upgrad.stackoverflow.service.exception.InvalidQuestionException;
import com.upgrad.stackoverflow.service.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.TypedQuery;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RequestMapping("/question")
public class QuestionController {

    @Autowired
    private QuestionBusinessService questionBusinessService;

    @PostMapping("/question/create")
    public ResponseEntity<QuestionResponse> createQuestion(@RequestBody QuestionEntity questionRequest, @RequestHeader(value="Authorization") String authorization) throws AuthorizationFailedException
    {

        QuestionEntity question = questionBusinessService.createQuestion(questionRequest, authorization);
        QuestionResponse questionResponse = new QuestionResponse();
        questionResponse.setId(question.getUuid());
        questionResponse.setStatus("Question created");
        return new ResponseEntity<>(questionResponse, HttpStatus.OK);
    }
    /**
     * A controller method to fetch all the questions from the database.
     *
     * @param authorization - A field in the request header which contains the JWT token.
     * @return - ResponseEntity<List<QuestionDetailsResponse>> type object along with Http status OK.
     * @throws AuthorizationFailedException
     */
     @GetMapping("/question/all")
    public ResponseEntity<List<QuestionDetailsResponse>> getQuestions(String authorization) throws AuthorizationFailedException
     {
         TypedQuery<QuestionEntity> questionList = questionBusinessService.getQuestions(authorization);
         List<QuestionEntity> resultList = questionList.getResultList();
         List<QuestionDetailsResponse> responseList = resultList.stream()
                 .map(question -> {
                     QuestionDetailsResponse response = new QuestionDetailsResponse();
                     response.setContent(question.getContent());
                     response.setId(question.getUuid());
                     return response;
                 }).collect(Collectors.toList());

         return new ResponseEntity<>(responseList, HttpStatus.OK);
     }

    /**
     * A controller method to edit the question in the database.
     *
     * @param questionEditRequest - This argument contains all the attributes required to edit the question details in the database.
     * @param questionId          - The uuid of the question to be edited in the database.
     * @param authorization       - A field in the request header which contains the JWT token.
     * @return - ResponseEntity<QuestionEditResponse> type object along with Http status OK.
     * @throws AuthorizationFailedException
     * @throws InvalidQuestionException
     */
    @PutMapping(path = "edit/{questionId}")
    public ResponseEntity<QuestionEditResponse> editQuestionContent(@PathVariable("questionId") String questionId, @RequestBody QuestionEditRequest questionEditRequest, @RequestHeader("authorization") String authorization) throws AuthorizationFailedException, InvalidQuestionException
    {
        QuestionEditResponse question = new QuestionEditResponse();
        QuestionEntity questionContent = questionBusinessService.editQuestionContent(questionEditRequest.getContent(), questionId, authorization);
        question.id(questionContent.getUuid());
        question.status("QUESTION EDITED");
        return new ResponseEntity<>(question, HttpStatus.OK);
    }


    /**
     * A controller method to delete the question in the database.
     *
     * @param questionId    - The uuid of the question to be deleted in the database.
     * @param authorization - A field in the request header which contains the JWT token.
     * @return - ResponseEntity<QuestionDeleteResponse> type object along with Http status OK.
     * @throws AuthorizationFailedException
     * @throws InvalidQuestionException
     */

    /**
     * A controller method to fetch all the questions posted by a specific user.
     *
     * @param userId        - The uuid of the user whose questions are to be fetched from the database.
     * @param authorization - A field in the request header which contains the JWT token.
     * @return - ResponseEntity<List<QuestionDetailsResponse>> type object along with Http status OK.
     * @throws AuthorizationFailedException
     * @throws UserNotFoundException
     */
}
