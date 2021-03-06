package com.teachit.web.rest;

import com.teachit.TeachitApp;
import com.teachit.domain.DiscursiveAnswer;
import com.teachit.repository.DiscursiveAnswerRepository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.hamcrest.Matchers.hasItem;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


/**
 * Test class for the DiscursiveAnswerResource REST controller.
 *
 * @see DiscursiveAnswerResource
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = TeachitApp.class)
@WebAppConfiguration
@IntegrationTest
public class DiscursiveAnswerResourceIntTest {

    private static final String DEFAULT_ANSWER = "AAAAA";
    private static final String UPDATED_ANSWER = "BBBBB";

    private static final Double DEFAULT_SCORE = 1D;
    private static final Double UPDATED_SCORE = 2D;

    @Inject
    private DiscursiveAnswerRepository discursiveAnswerRepository;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    private MockMvc restDiscursiveAnswerMockMvc;

    private DiscursiveAnswer discursiveAnswer;

    @PostConstruct
    public void setup() {
        MockitoAnnotations.initMocks(this);
        DiscursiveAnswerResource discursiveAnswerResource = new DiscursiveAnswerResource();
        ReflectionTestUtils.setField(discursiveAnswerResource, "discursiveAnswerRepository", discursiveAnswerRepository);
        this.restDiscursiveAnswerMockMvc = MockMvcBuilders.standaloneSetup(discursiveAnswerResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    @Before
    public void initTest() {
        discursiveAnswer = new DiscursiveAnswer();
        discursiveAnswer.setAnswer(DEFAULT_ANSWER);
        discursiveAnswer.setScore(DEFAULT_SCORE);
    }

    @Test
    @Transactional
    public void createDiscursiveAnswer() throws Exception {
        int databaseSizeBeforeCreate = discursiveAnswerRepository.findAll().size();

        // Create the DiscursiveAnswer

        restDiscursiveAnswerMockMvc.perform(post("/api/discursive-answers")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(discursiveAnswer)))
                .andExpect(status().isCreated());

        // Validate the DiscursiveAnswer in the database
        List<DiscursiveAnswer> discursiveAnswers = discursiveAnswerRepository.findAll();
        assertThat(discursiveAnswers).hasSize(databaseSizeBeforeCreate + 1);
        DiscursiveAnswer testDiscursiveAnswer = discursiveAnswers.get(discursiveAnswers.size() - 1);
        assertThat(testDiscursiveAnswer.getAnswer()).isEqualTo(DEFAULT_ANSWER);
        assertThat(testDiscursiveAnswer.getScore()).isEqualTo(DEFAULT_SCORE);
    }

    @Test
    @Transactional
    public void getAllDiscursiveAnswers() throws Exception {
        // Initialize the database
        discursiveAnswerRepository.saveAndFlush(discursiveAnswer);

        // Get all the discursiveAnswers
        restDiscursiveAnswerMockMvc.perform(get("/api/discursive-answers?sort=id,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[*].id").value(hasItem(discursiveAnswer.getId().intValue())))
                .andExpect(jsonPath("$.[*].answer").value(hasItem(DEFAULT_ANSWER.toString())))
                .andExpect(jsonPath("$.[*].score").value(hasItem(DEFAULT_SCORE.doubleValue())));
    }

    @Test
    @Transactional
    public void getDiscursiveAnswer() throws Exception {
        // Initialize the database
        discursiveAnswerRepository.saveAndFlush(discursiveAnswer);

        // Get the discursiveAnswer
        restDiscursiveAnswerMockMvc.perform(get("/api/discursive-answers/{id}", discursiveAnswer.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(discursiveAnswer.getId().intValue()))
            .andExpect(jsonPath("$.answer").value(DEFAULT_ANSWER.toString()))
            .andExpect(jsonPath("$.score").value(DEFAULT_SCORE.doubleValue()));
    }

    @Test
    @Transactional
    public void getNonExistingDiscursiveAnswer() throws Exception {
        // Get the discursiveAnswer
        restDiscursiveAnswerMockMvc.perform(get("/api/discursive-answers/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateDiscursiveAnswer() throws Exception {
        // Initialize the database
        discursiveAnswerRepository.saveAndFlush(discursiveAnswer);
        int databaseSizeBeforeUpdate = discursiveAnswerRepository.findAll().size();

        // Update the discursiveAnswer
        DiscursiveAnswer updatedDiscursiveAnswer = new DiscursiveAnswer();
        updatedDiscursiveAnswer.setId(discursiveAnswer.getId());
        updatedDiscursiveAnswer.setAnswer(UPDATED_ANSWER);
        updatedDiscursiveAnswer.setScore(UPDATED_SCORE);

        restDiscursiveAnswerMockMvc.perform(put("/api/discursive-answers")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(updatedDiscursiveAnswer)))
                .andExpect(status().isOk());

        // Validate the DiscursiveAnswer in the database
        List<DiscursiveAnswer> discursiveAnswers = discursiveAnswerRepository.findAll();
        assertThat(discursiveAnswers).hasSize(databaseSizeBeforeUpdate);
        DiscursiveAnswer testDiscursiveAnswer = discursiveAnswers.get(discursiveAnswers.size() - 1);
        assertThat(testDiscursiveAnswer.getAnswer()).isEqualTo(UPDATED_ANSWER);
        assertThat(testDiscursiveAnswer.getScore()).isEqualTo(UPDATED_SCORE);
    }

    @Test
    @Transactional
    public void deleteDiscursiveAnswer() throws Exception {
        // Initialize the database
        discursiveAnswerRepository.saveAndFlush(discursiveAnswer);
        int databaseSizeBeforeDelete = discursiveAnswerRepository.findAll().size();

        // Get the discursiveAnswer
        restDiscursiveAnswerMockMvc.perform(delete("/api/discursive-answers/{id}", discursiveAnswer.getId())
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Validate the database is empty
        List<DiscursiveAnswer> discursiveAnswers = discursiveAnswerRepository.findAll();
        assertThat(discursiveAnswers).hasSize(databaseSizeBeforeDelete - 1);
    }
}
