"""
FlowDesk ML Service - Flask API
File: ml-service/app.py
"""

from flask import Flask, request, jsonify
from flask_cors import CORS
import joblib
import numpy as np
from sentence_transformers import SentenceTransformer
from sklearn.metrics.pairwise import cosine_similarity
import os

app = Flask(__name__)
CORS(app)

# ============================================================================
# LOAD MODELS ON STARTUP
# ============================================================================

print("🔄 Loading ML models...")

MODEL_DIR = "models"

# Load Risk Prediction Model
risk_model = joblib.load(f"{MODEL_DIR}/risk_model.pkl")
label_encoder = joblib.load(f"{MODEL_DIR}/label_encoder.pkl")
feature_names = joblib.load(f"{MODEL_DIR}/feature_names.pkl")

# Load Assignee Recommendation Model
assignee_model = joblib.load(f"{MODEL_DIR}/assignee_model.pkl")

# Load Sentence Transformer
embedding_model = SentenceTransformer(f"{MODEL_DIR}/embedding_model")

print("✅ All models loaded successfully!")
print(f"   Risk model classes: {label_encoder.classes_}")
print(f"   Feature names: {feature_names}")
print(f"   Embedding dimension: {embedding_model.get_sentence_embedding_dimension()}")

# ============================================================================
# ENDPOINT 1: PREDICT TASK RISK
# ============================================================================

@app.route('/api/ml/predict-risk', methods=['POST'])
def predict_risk():
    """Predicts task completion risk"""
    try:
        data = request.get_json()
        
        # Extract features
        estimated = float(data.get('estimatedHours', 0))
        story_points = int(data.get('storyPoints', 0))
        workload = float(data.get('assignedToWorkload', 0))
        
        # Map priority enum to numeric
        priority_map = {'CRITICAL': 1, 'HIGH': 2, 'MEDIUM': 3, 'LOW': 4}
        priority_str = data.get('priority', 'MEDIUM')
        priority = priority_map.get(priority_str, 3)
        
        subtasks = int(data.get('subtaskCount', 0))
        age_days = int(data.get('taskAgeDays', 0))
        
        # Create feature vector
        features = np.array([[estimated, story_points, workload, priority, subtasks, age_days]])
        
        # Predict
        risk_pred = risk_model.predict(features)[0]
        risk_proba = risk_model.predict_proba(features)[0]
        
        # Decode risk level
        risk_level = label_encoder.inverse_transform([risk_pred])[0]
        
        # Calculate confidence
        max_proba = np.max(risk_proba)
        confidence = "HIGH" if max_proba > 0.7 else "MEDIUM" if max_proba > 0.5 else "LOW"
        
        # Build probability dict
        prob_dict = {
            label: float(prob) 
            for label, prob in zip(label_encoder.classes_, risk_proba)
        }
        
        response = {
            "riskLevel": risk_level,
            "riskScore": float(max_proba),
            "willMissDeadline": risk_level == "HIGH",
            "confidence": confidence,
            "probabilities": prob_dict,
            "lastUpdated": None
        }
        
        print(f"✅ Risk prediction: {risk_level} (confidence: {confidence})")
        return jsonify(response), 200
        
    except Exception as e:
        print(f"❌ Error in predict_risk: {str(e)}")
        return jsonify({"error": str(e)}), 500


# ============================================================================
# ENDPOINT 2: RECOMMEND ASSIGNEES
# ============================================================================

@app.route('/api/ml/recommend-assignees', methods=['POST'])
def recommend_assignees():
    """Recommends top developers for a task"""
    try:
        data = request.get_json()
        
        task_description = data.get('taskDescription', '')
        task_skills = data.get('taskSkills', [])
        developers = data.get('developers', [])
        
        if not developers:
            return jsonify([]), 200
        
        # Generate task embedding
        task_text = f"{task_description} {' '.join(task_skills)}"
        task_embedding = embedding_model.encode([task_text])
        
        recommendations = []
        
        for dev in developers:
            # Generate developer skill embedding
            dev_skills_text = ' '.join(dev.get('skills', []))
            dev_embedding = embedding_model.encode([dev_skills_text])
            
            # Calculate skill match score
            skill_match = cosine_similarity(task_embedding, dev_embedding)[0][0]
            skill_match_score = float(skill_match * 100)
            
            # Calculate workload score
            current = dev.get('currentWorkload', 0)
            max_cap = dev.get('maxCapacity', 40)
            workload_free = max_cap - current
            
            # Prepare features for assignee model
            completion_rate = dev.get('completionRate', 0.8)
            avg_duration = dev.get('avgTaskDuration', 10)
            
            assignee_features = np.array([[
                skill_match_score,
                workload_free,
                completion_rate,
                avg_duration
            ]])
            
            # Predict overall score
            overall_score = assignee_model.predict(assignee_features)[0]
            
            recommendations.append({
                "userId": dev.get('userId'),
                "skillMatchScore": skill_match_score / 100,
                "workloadScore": float(workload_free / max_cap),
                "overallScore": float(overall_score / 100),
                "matchPercentage": int(overall_score)
            })
        
        # Sort and return top 3
        recommendations.sort(key=lambda x: x['overallScore'], reverse=True)
        top_recommendations = recommendations[:3]
        
        print(f"✅ Generated {len(top_recommendations)} recommendations")
        return jsonify(top_recommendations), 200
        
    except Exception as e:
        print(f"❌ Error in recommend_assignees: {str(e)}")
        return jsonify({"error": str(e)}), 500


# ============================================================================
# ENDPOINT 3: GENERATE TASK SUMMARY
# ============================================================================

@app.route('/api/ml/generate-summary', methods=['POST'])
def generate_summary():
    """Generates AI summary of completed task"""
    try:
        data = request.get_json()
        
        title = data.get('taskTitle', '')
        description = data.get('taskDescription', '')
        subtasks = data.get('subtasks', [])
        comments = data.get('comments', [])
        actual = data.get('actualHours', 0)
        estimated = data.get('estimatedHours', 0)
        
        # Build summary
        subtasks_text = ", ".join(subtasks) if subtasks else "no subtasks"
        comments_text = " ".join(comments) if comments else ""
        
        summary = f"The task '{title}' involved {description.lower()}. Work included: {subtasks_text}. "
        
        if actual and estimated:
            if actual <= estimated:
                summary += f"Completed efficiently in {actual} hours (estimated {estimated} hours). "
            else:
                summary += f"Took {actual} hours, exceeding the {estimated} hour estimate. "
        
        if comments_text:
            summary += f"Developer notes: {comments_text[:200]}."
        
        summary = summary[:500]
        
        print(f"✅ Generated summary: {len(summary)} characters")
        return jsonify(summary), 200
        
    except Exception as e:
        print(f"❌ Error in generate_summary: {str(e)}")
        return jsonify({"error": str(e)}), 500


# ============================================================================
# HEALTH CHECK
# ============================================================================

@app.route('/health', methods=['GET'])
def health_check():
    """Health check endpoint"""
    return jsonify({
        "status": "healthy",
        "service": "FlowDesk ML Service",
        "models_loaded": True
    }), 200


# ============================================================================
# RUN SERVER
# ============================================================================

if __name__ == '__main__':
    print("\n" + "="*60)
    print("🚀 FlowDesk ML Service Starting...")
    print("="*60)
    print("📡 Endpoints:")
    print("   POST http://localhost:5000/api/ml/predict-risk")
    print("   POST http://localhost:5000/api/ml/recommend-assignees")
    print("   POST http://localhost:5000/api/ml/generate-summary")
    print("   GET  http://localhost:5000/health")
    print("="*60 + "\n")
    
    app.run(host='0.0.0.0', port=5000, debug=True)

