fetch("/videos/ips")
  .then(response => response.json())
  .then(data => {
    console.log(data);  
    data.forEach(ip => {
      let endpoint = "http://" + ip + ":8080/videos";
      fetch(endpoint)
        .then(res => res.json())
        .then(files => {
          const container = document.getElementById("video-container");

          files.forEach(file => {
            const block = document.createElement("div");
            block.className = "video-block";

            const title = document.createElement("button");
            title.onclick = function goToVideo() {
              window.location.href = `/video.html?file=${encodeURIComponent(file)}`;
            }
            title.textContent = file;

            block.appendChild(title);
            block.appendChild(document.createElement("br"));

            container.appendChild(block);
          });
        })
        .catch(err => console.error(`Error fetching videos from ${endpoint}:`, err));
    });
  })
  .catch(error => console.error("Error fetching IPs:", error));
