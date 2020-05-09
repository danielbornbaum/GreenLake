const path = require('path');

module.exports = {
    entry: './src/main/webapp/scripts/App.js',
    output: {
        filename: 'data-processing.bundle.js',
        path: path.resolve(__dirname, 'src/main/webapp/bundle/'),
    },
    module: {
        rules: [
            {
                test: /\.m?js$/,
                exclude: /(node_modules|node)/,
                use: {
                    loader: "babel-loader",
                    options: {
                        presets: ['@babel/preset-react']
                    }
                }
            },
            {
                test: /\.css$/,
                use: [
                    'style-loader',
                    'css-loader'
                ]
            }
        ]
    },
    resolve: {
        extensions: ['.js', '.jsx']
    }
};