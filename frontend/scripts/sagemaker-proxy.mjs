import http from "node:http";

const LISTEN_PORT = 3000;
const NEXT_PORT = 3001;
const PREFIX = "/codeeditor/default";

const server = http.createServer((req, res) => {
  const url = `${PREFIX}${req.url}`;

  const proxyReq = http.request(
    {
      hostname: "127.0.0.1",
      port: NEXT_PORT,
      path: url,
      method: req.method,
      headers: req.headers,
    },
    (proxyRes) => {
      res.writeHead(proxyRes.statusCode ?? 500, proxyRes.headers);
      proxyRes.pipe(res);
    }
  );

  proxyReq.on("error", (err) => {
    console.error("Proxy error:", err.message);
    res.writeHead(502);
    res.end("Bad Gateway");
  });

  req.pipe(proxyReq);
});

server.listen(LISTEN_PORT, () => {
  console.log(`SageMaker proxy listening on :${LISTEN_PORT} -> :${NEXT_PORT}`);
});
