 document.addEventListener("DOMContentLoaded", function(){
            let params = new URLSearchParams(window.location.search);
            let file = params.get('file');

            const url = `/videos/stream/${encodeURIComponent(file)}`;
            fetch(url, {method: 'HEAD'})
            .then(response => {
                if (response.ok) {
                    document.getElementById("title").innerText = file;

                    const video = document.createElement("video");
                    video.controls = true;

                    video.src = `/videos/stream/${encodeURIComponent(file)}`;
                    video.classList.add("video");

                    const container = document.getElementById("video-container");

                    const downloadLink = document.createElement("a");
                    downloadLink.href = `/videos/download/${encodeURIComponent(file)}`;
                    downloadLink.textContent = "⬇ Download Video";
                    downloadLink.download = file;

                    const block = document.createElement("div");
                    block.className = "video-block";

                    block.appendChild(video);
                    block.appendChild(document.createElement("br"));
                    block.appendChild(downloadLink);
                    container.appendChild(block);
                }
                else{
                    document.getElementById("title").innerText = "This file does not exist!";
                } 
            })
        })