import psycopg2
from langchain_community.embeddings import OllamaEmbeddings
import json
import numpy as np
import requests

# Test Ollama server
try:
    response = requests.get("http://localhost:11434")
    if response.status_code != 200:
        print(f"❌ Ollama server not running: {response.status_code}")
        exit(1)
    print("✅ Ollama server is running")
except Exception as e:
    print(f"❌ Failed to connect to Ollama: {e}")
    exit(1)

# Initialize the embedding model
try:
    embedding_model = OllamaEmbeddings(model="jeffh/intfloat-multilingual-e5-small:f32")
    print("✅ Embedding model initialized")
    test_vec = embedding_model.embed_query('{"english": "Test description"}')
    print(f"✅ Test embedding: length={len(test_vec)}, first 5 values={test_vec[:5]}")
    if np.allclose(test_vec, 0):
        print("❌ Test embedding is all zeros")
        exit(1)
except Exception as e:
    print(f"❌ Failed to initialize embedding model: {e}")
    exit(1)

# Connect to PostgreSQL
try:
    conn = psycopg2.connect(
        dbname="crm_project",
        user="postgres",
        password="postgres",
        host="localhost",
        port="5432"
    )
    cur = conn.cursor()
    print("✅ Connected to PostgreSQL database")
except Exception as e:
    print(f"❌ Failed to connect to database: {e}")
    exit(1)

# Verify table schema
try:
    cur.execute("SELECT column_name, data_type FROM information_schema.columns WHERE table_name = 'product' AND column_name = 'rag_vector';")
    schema = cur.fetchone()
    if schema and schema[1] != 'vector':
        print(f"❌ Invalid rag_vector type: {schema[1]} (expected 'vector')")
        cur.close()
        conn.close()
        exit(1)
    print("✅ Verified rag_vector column type")
except Exception as e:
    print(f"❌ Error checking schema: {e}")
    cur.close()
    conn.close()
    exit(1)

# Fetch product descriptions
try:
    cur.execute("SELECT id, description FROM product;")
    rows = cur.fetchall()
    print(f"✅ Retrieved {len(rows)} products")
    if len(rows) == 0:
        print("⚠️ Product table is empty")
        cur.close()
        conn.close()
        exit(1)
except Exception as e:
    print(f"❌ Error fetching products: {e}")
    cur.close()
    conn.close()
    exit(1)

# Generate embeddings and update rag_vector
for prod_id, description in rows:
    if description:
        try:
            # Parse JSON description
            json_desc = json.loads(description)
            print(f"✅ Valid JSON for product ID {prod_id}: {description[:50]}...")
            # Generate embedding
            vec = embedding_model.embed_query(description)
            # Verify embedding
            if len(vec) != 384:
                print(f"❌ Unexpected embedding dimension for product {prod_id}: {len(vec)}")
                continue
            if np.allclose(vec, 0):
                print(f"❌ Zero vector generated for product ID {prod_id}")
                continue
            # Update rag_vector
            cur.execute(
                "UPDATE product SET rag_vector = %s WHERE id = %s;",
                (vec, prod_id)
            )
            print(f"✅ Updated rag_vector for product ID {prod_id}: first 5 values={vec[:5]}")
        except json.JSONDecodeError as e:
            print(f"❌ Invalid JSON for product ID {prod_id}: {e}")
        except Exception as e:
            print(f"❌ Error processing product ID {prod_id}: {e}")
    else:
        print(f"⚠️ Skipping product ID {prod_id}: Empty description")

# Commit and close
try:
    conn.commit()
    print("✅ Changes committed to database")
except Exception as e:
    print(f"❌ Error committing changes: {e}")
finally:
    cur.close()
    conn.close()
    print("✅ Database connection closed")
print("✅ Vector population completed!")