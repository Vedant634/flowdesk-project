"""
ML Service - Machine Learning model inference
Handles loading models and making predictions
"""
import joblib
import numpy as np
from sentence_transformers import SentenceTransformer
from sklearn.metrics.pairwise import cosine_similarity
import logging
import os
import config

logger = logging.getLogger(__name__)

class MLService:
    """Service for ML model predictions"""
    
    # Class variables to store loaded models
    risk_model = None
    label_encoder = None
    assignee_model = None
    feature_names = None
    embedding_model = None
    
    @classmethod
    def load_models(cls):
        """Load all ML models from disk"""
        try:
            model_path = config.MODEL_PATH
            
            # Load risk prediction model
            logger.info("Loading risk model...")
            cls.risk_model = joblib.load(
                os.path.join(model_path, config.RISK_MODEL_FILE)
            )
            
            # Load label encoder
            logger.info("Loading label encoder...")
            cls.label_encoder = joblib.load(
                os.path.join(model_path, config.LABEL_ENCODER_FILE)
            )
            
            # Load assignee recommendation model
            logger.info("Loading assignee model...")
            cls.assignee_model = joblib.load(
                os.path.join(model_path, config.ASSIGNEE_MODEL_FILE)
            )
            
            # Load feature names
            logger.info("Loading feature names...")
            cls.feature_names = joblib.load(
                os.path.join(model_path, config.FEATURE_NAMES_FILE)
            )
            
            # Load sentence transformer for embeddings
            logger.info("Loading embedding model...")
            embedding_path = os.path.join(model_path, config.EMBEDDING_MODEL_DIR)
            cls.embedding_model = SentenceTransformer(embedding_path)
            
            logger.info("✅ All models loaded successfully!")
            
        except Exception as e:
            logger.error(f"❌ Failed to load models: {str(e)}")
            raise
    
    @classmethod
    def models_loaded(cls):
        """Check if all models are loaded"""
        return all([
            cls.risk_model is not None,
            cls.label_encoder is not None,
            cls.assignee_model is not None,
            cls.feature_names is not None,
            cls.embedding_model is not None
        ])
    
    @classmethod
    def predict_risk(cls, estimated_hours, story_points, developer_workload, 
                     priority, num_subtasks, task_age_days):
        """
        Predict task risk level
        
        Returns:
            dict: {
                "risk_level": "HIGH",
                "risk_score": 85,
                "probabilities": {"LOW": 0.05, "MEDIUM": 0.15, "HIGH": 0.80},
                "confidence": "HIGH"
            }
        """
        if not cls.models_loaded():
            raise RuntimeError("Models not loaded. Call load_models() first.")
        
        try:
            # Prepare feature vector
            features = np.array([[
                estimated_hours,
                story_points,
                developer_workload,
                priority,
                num_subtasks,
                task_age_days
            ]])
            
            # Get prediction
            prediction = cls.risk_model.predict(features)[0]
            probabilities = cls.risk_model.predict_proba(features)[0]
            
            # Decode label
            risk_level = cls.label_encoder.inverse_transform([prediction])[0]
            
            # Calculate risk score (0-100)
            risk_score = int(probabilities.max() * 100)
            
            # Determine confidence
            max_prob = probabilities.max()
            if max_prob > 0.8:
                confidence = "HIGH"
            elif max_prob > 0.6:
                confidence = "MEDIUM"
            else:
                confidence = "LOW"
            
            # Create probability dict
            prob_dict = {
                cls.label_encoder.classes_[i]: round(float(probabilities[i]), 3)
                for i in range(len(cls.label_encoder.classes_))
            }
            
            return {
                "risk_level": risk_level,
                "risk_score": risk_score,
                "probabilities": prob_dict,
                "confidence": confidence,
                "factors": {
                    "estimated_hours": estimated_hours,
                    "story_points": story_points,
                    "developer_workload": developer_workload,
                    "priority": priority
                }
            }
            
        except Exception as e:
            logger.error(f"Risk prediction error: {str(e)}")
            raise
    
    @classmethod
    def recommend_assignee(cls, task_skills, developers):
        """
        Recommend best developer(s) for task
        
        Returns:
            list: [
                {
                    "developer_id": "uuid1",
                    "name": "John Doe",
                    "skill_match_score": 85,
                    "recommendation_score": 92,
                    "confidence": "HIGH",
                    "workload_availability": 50,
                    "reasoning": "..."
                }
            ]
        """
        if not cls.models_loaded():
            raise RuntimeError("Models not loaded. Call load_models() first.")
        
        try:
            recommendations = []
            
            # Encode task skills
            task_skills_text = " ".join(task_skills)
            task_embedding = cls.embedding_model.encode([task_skills_text])
            
            for dev in developers:
                # Encode developer skills
                dev_skills_text = " ".join(dev['skills'])
                dev_embedding = cls.embedding_model.encode([dev_skills_text])
                
                # Calculate skill similarity
                skill_similarity = cosine_similarity(task_embedding, dev_embedding)[0][0]
                skill_match_score = int(skill_similarity * 100)
                
                # Calculate workload availability
                workload_free = dev['max_capacity'] - dev['current_workload']
                workload_pct = (workload_free / dev['max_capacity']) * 100
                
                # Prepare features for assignee model
                features = np.array([[
                    skill_match_score,
                    workload_free,
                    dev['completion_rate'],
                    dev['avg_task_duration']
                ]])
                
                # Get recommendation score
                rec_score = cls.assignee_model.predict(features)[0]
                
                # Determine confidence
                if rec_score > 80:
                    confidence = "HIGH"
                elif rec_score > 60:
                    confidence = "MEDIUM"
                else:
                    confidence = "LOW"
                
                # Generate reasoning
                reasoning = cls._generate_reasoning(
                    skill_match_score, workload_pct, 
                    dev['completion_rate'], dev['avg_task_duration']
                )
                
                recommendations.append({
                    "developer_id": dev['id'],
                    "name": dev['name'],
                    "skill_match_score": skill_match_score,
                    "recommendation_score": int(rec_score),
                    "confidence": confidence,
                    "workload_availability": int(workload_pct),
                    "completion_rate": dev['completion_rate'],
                    "avg_task_duration": dev['avg_task_duration'],
                    "reasoning": reasoning
                })
            
            # Sort by recommendation score (descending)
            recommendations.sort(key=lambda x: x['recommendation_score'], reverse=True)
            
            return recommendations
            
        except Exception as e:
            logger.error(f"Assignee recommendation error: {str(e)}")
            raise
    
    @classmethod
    def _generate_reasoning(cls, skill_match, workload_avail, completion_rate, avg_duration):
        """Generate human-readable reasoning for recommendation"""
        reasons = []
        
        if skill_match > 80:
            reasons.append(f"Excellent skill match ({skill_match}%)")
        elif skill_match > 60:
            reasons.append(f"Good skill match ({skill_match}%)")
        else:
            reasons.append(f"Moderate skill match ({skill_match}%)")
        
        if workload_avail > 70:
            reasons.append("high availability")
        elif workload_avail > 40:
            reasons.append("moderate availability")
        else:
            reasons.append("limited availability")
        
        if completion_rate > 0.9:
            reasons.append("excellent track record")
        elif completion_rate > 0.75:
            reasons.append("good track record")
        
        if avg_duration < 7:
            reasons.append("fast task completion")
        
        return ", ".join(reasons).capitalize()
    
    @classmethod
    def generate_summary(cls, title, description, task_type):
        """
        Generate AI summary for task (placeholder for now)
        
        Returns:
            dict: {
                "summary": "...",
                "suggested_subtasks": [...],
                "estimated_complexity": "MEDIUM"
            }
        """
        # This is a placeholder - can be enhanced with GPT/LLM later
        summary = f"Task: {title}. "
        if description:
            summary += f"Details: {description[:100]}..."
        
        # Generate simple subtasks based on type
        subtasks = []
        if task_type == "FEATURE":
            subtasks = [
                "Design and plan implementation",
                "Implement core functionality",
                "Write unit tests",
                "Code review and testing"
            ]
        elif task_type == "BUG":
            subtasks = [
                "Reproduce and investigate bug",
                "Implement fix",
                "Test fix thoroughly",
                "Deploy and verify"
            ]
        
        # Simple complexity estimation
        complexity = "LOW"
        if len(description) > 200 or len(title.split()) > 5:
            complexity = "MEDIUM"
        if len(description) > 500 or "integration" in title.lower():
            complexity = "HIGH"
        
        return {
            "summary": summary,
            "suggested_subtasks": subtasks,
            "estimated_complexity": complexity,
            "task_type": task_type
        }
