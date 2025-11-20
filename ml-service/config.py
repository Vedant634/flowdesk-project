"""
Configuration for FlowDesk ML Service
"""
import os

# Model paths
MODEL_PATH = 'models/'
RISK_MODEL_FILE = 'risk_model.pkl'
LABEL_ENCODER_FILE = 'label_encoder.pkl'
ASSIGNEE_MODEL_FILE = 'assignee_model.pkl'
FEATURE_NAMES_FILE = 'feature_names.pkl'
EMBEDDING_MODEL_DIR = 'embedding_model'

# Server configuration
PORT = int(os.getenv('PORT', 5000))
DEBUG = os.getenv('FLASK_ENV', 'development') == 'development'

# CORS settings
CORS_ORIGINS = os.getenv('CORS_ORIGINS', '*')

# Logging
LOG_LEVEL = os.getenv('LOG_LEVEL', 'INFO')
