from __future__ import annotations

import base64
import logging
import os
from urllib.parse import quote

import requests

logger = logging.getLogger("pipeline_trigger")


class AdoPipelineClient:
    def __init__(self, org: str, project: str, pipeline_id: str, pat: str):
        self.org = org
        self.project = project
        self.pipeline_id = pipeline_id
        token = base64.b64encode(f":{pat}".encode()).decode()
        self._headers = {
            "Authorization": f"Basic {token}",
            "Content-Type": "application/json",
        }

    @classmethod
    def from_env(cls) -> "AdoPipelineClient":
        return cls(
            org=os.environ["ADO_ORG"],
            project=os.environ["ADO_PROJECT"],
            pipeline_id=os.environ["ADO_PIPELINE_ID"],
            pat=os.environ["ADO_PAT"],
        )

    def run(self, doc_id: str) -> dict:
        url = (
            f"https://dev.azure.com/{self.org}/{quote(self.project)}"
            f"/_apis/pipelines/{self.pipeline_id}/runs?api-version=7.1"
        )
        body = {"templateParameters": {"docId": doc_id}}
        resp = requests.post(url, json=body, headers=self._headers, timeout=30)
        resp.raise_for_status()
        data = resp.json()
        return {
            "id": data.get("id"),
            "url": data.get("_links", {}).get("web", {}).get("href"),
        }


def trigger_nft_pipeline(doc_id: str) -> dict | None:
    """Start the ADO NFT pipeline for one Cosmos run record. Never raises."""
    try:
        result = AdoPipelineClient.from_env().run(doc_id)
        logger.info("NFT pipeline started for %s: run %s (%s)",
                    doc_id, result["id"], result["url"])
        return result
    except Exception:
        logger.exception("Could not start NFT pipeline for %s", doc_id)
        return None